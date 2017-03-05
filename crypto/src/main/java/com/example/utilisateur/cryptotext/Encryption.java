package com.example.utilisateur.cryptotext;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * The keys are stocked in a file protected with a password which is to be given before opening and creating a conversation
 * @author DonatienTERTRAIS
 */
class Encryption {
    private static final String ENCRYPTION_IV = "4e5Wa71fYoT7MFEX";
    private static final String KEY_FILE_NAME = "CryptoKeys.keystore";

    /**
     * Creates a KeyStore file or loads it if was already created
     * @param context The context of the application
     * @param pwd Password of the KeyStore file
     */
    static void createKeyStore(Context context, String pwd) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, pwd.toCharArray());
            saveKeyStore(context, keyStore, pwd);
        } catch (Exception e) {
            checkExceptions2(e);
        }
    }

    /**
     * Gets the KeyStore
     * @param context The Context of the application
     * @param pwd The keyStore password
     * @return The KeyStore
     */
    private static KeyStore getKeyStore(Context context, String pwd) {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream fis = context.openFileInput(KEY_FILE_NAME);
            keyStore.load(fis, pwd.toCharArray());
            fis.close();
        }  catch (Exception e) {
            checkExceptions2(e);
        }
        return keyStore;
    }

    /**
     * Saves the KeyStore
     * @param context The context of the application
     * @param keyStore KeyStore to be saved
     * @param pwd  Password of the KeyStore
     */
    private static void saveKeyStore(Context context, KeyStore keyStore, String pwd) {
        try {
            FileOutputStream fos = context.openFileOutput(KEY_FILE_NAME, Context.MODE_PRIVATE);
            keyStore.store(fos, pwd.toCharArray());
            fos.close();
        } catch (Exception e) {
            checkExceptions2(e);
        }
    }

    /**
     * This function test a password on a keystore
     * @param pwd Password that is to be tried
     * @return True if the password is correct, false otherwise
     */
    static boolean testPassword(Context context, String pwd) {
        try {
            FileInputStream fis = context.openFileInput(KEY_FILE_NAME);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(fis, pwd.toCharArray());
            fis.close();
        } catch (IOException e) {
            Log.e("CT: wrong pwd on test", Log.getStackTraceString(e));
            return false;
        }  catch (Exception e) {
            Log.e("CT: Any Exc on pwd test", Log.getStackTraceString(e));
        }
        return true;
    }

    /**
     *Checks if the keystore file exists
     * @param context Context of the application
     * @return True if the file exists, false otherwise
     */
    static boolean exists(Context context) {
        File file = new File(context.getFilesDir(), KEY_FILE_NAME);
        if (file.exists()) {
            return true;
        } else {
            try {
                FileOutputStream fos = context.openFileOutput(KEY_FILE_NAME, Context.MODE_PRIVATE);
                fos.close();
                return false;
            } catch (FileNotFoundException e){
                Log.e("CT: File not found", Log.getStackTraceString(e));
            } catch (IOException e){
                Log.e("CT: IOEsc FI/OS close", Log.getStackTraceString(e));
            }
            return false;
        }
    }

    /**
     * Encrypts data and encodes it into Base64
     * @param key Key to be used
     * @param data data to be encrypted
     * @return Encrypted Data
     */
    static String encrypt( String key, String data ) {
        try {
            IvParameterSpec iv = new IvParameterSpec(ENCRYPTION_IV.getBytes("UTF-8"));
            byte[] clear = data.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec( key.getBytes(), "AES" );
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            byte[] encrypted = cipher.doFinal(clear);
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            Log.e("CT: imp. IV gen.", Log.getStackTraceString(e));
            return "";
        } catch (Exception e) {
            checkExceptions1 (e);
            return "";
        }
    }

    /**
     * Decrypts data
     * @param key Key to be used
     * @param encryptedData Data to be Decrypted
     * @return Decrypted data
     */
    static String decrypt( String key, String encryptedData ) {
        IvParameterSpec iv;
        SecretKeySpec secretKeySpec = new SecretKeySpec( key.getBytes(), "AES" );
        byte[] decrypted = new byte[]{};
        try {
            iv = new IvParameterSpec(ENCRYPTION_IV.getBytes("UTF-8"));
            Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
            cipher.init( Cipher.DECRYPT_MODE, secretKeySpec, iv);

            byte[] encrypted = Base64.decode(encryptedData, Base64.DEFAULT);
            decrypted = cipher.doFinal(encrypted);
        } catch (Exception e) {
            checkExceptions1 (e);
        }
        return new String(decrypted);
    }

    /**
     * Generate a key from a given seed
     * @param seed Seed of the key
     * @return Key generated
     */
    private static SecretKey generateKey( byte[] seed ) {
        KeyGenerator keyGenerator;
        SecureRandom secureRandom;
        try {
            keyGenerator = KeyGenerator.getInstance( "AES" );
            secureRandom = SecureRandom.getInstance( "SHA1PRNG" );
            secureRandom.setSeed( seed );
            keyGenerator.init( 256, secureRandom );
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            Log.e("CT: PRNG unk. algo", Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Checked if a given entry is used to stock a key
     * @param context The context of the application
     * @param entry Entry to be checked
     * @param pwd Password of the KeyStore file
     * @return Whether the entry is stocked or not
     */
    static boolean isStocked(Context context, String entry, String pwd) {
        KeyStore keyStore = getKeyStore(context, pwd);
        boolean cond = false;
        try {
            cond = keyStore.containsAlias(entry);
        } catch (KeyStoreException e) {
            Log.e("CT: ks not init.", Log.getStackTraceString(e));
        }
        return cond;
    }

    /**
     * Get a key from an alias
     * @param context The context of the application
     * @param alias Alias of the searched key
     * @param pwd Password of the KeyStore file
     * @return The searched key
     */
    static String getKey(Context context, String alias, String pwd) {
        KeyStore keyStore = getKeyStore(context, pwd);

        String key = "";
        if (keyStore == null) {
            return null;
        }
        try {
            key = keyStore.getKey(alias, pwd.toCharArray()).toString();
        } catch (KeyStoreException e) {
            Log.e("CT: ks not init", Log.getStackTraceString(e));
        } catch (NoSuchAlgorithmException e) {
            Log.e("CT: no algo find key", Log.getStackTraceString(e));
        } catch (UnrecoverableKeyException e) {
            Log.e("CT: key unrecover.", Log.getStackTraceString(e));
        }
        return key;
    }

    /**
     * Saves a key from a seed
     * @param context The context of the application
     * @param seed Seed to be used
     * @param pwd Password of the Keystore file
     * @param alias Alias to be given to the Key
     */
    static void saveKey(Context context, String seed, String pwd, String alias) {
        KeyStore keyStore = getKeyStore(context, pwd);
        if (keyStore == null) {
            return;
        }
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(pwd.toCharArray());
        try {
            if (seed.length() != 0) {
                KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(Encryption.generateKey(seed.getBytes()));
                keyStore.setEntry(alias, keyStoreEntry, passwordProtection);
            }
        } catch (KeyStoreException e) {
            Log.e("CT: ks not init.", Log.getStackTraceString(e));
        }

        saveKeyStore(context, keyStore, pwd);
    }

    /**
     * Check an exception received when encrypting or decrypting data
     * @param e Exception to be checked
     */
    private static void checkExceptions1(Exception e){
        if (e instanceof NoSuchAlgorithmException) {
            Log.e("CT: no provider transfo", Log.getStackTraceString(e));
        } if (e instanceof NoSuchPaddingException) {
            Log.e("CT: unavail. pad scheme", Log.getStackTraceString(e));
        } if (e instanceof InvalidKeyException) {
            Log.e("CT: cipher nor init.", Log.getStackTraceString(e));
        } if (e instanceof IllegalBlockSizeException) {
            Log.e("CT: err on blocksize", Log.getStackTraceString(e));
        } if (e instanceof BadPaddingException) {
            Log.e("CT: padd bytes bound", Log.getStackTraceString(e));
        } if  (e instanceof UnsupportedEncodingException) {
            Log.e("CT: imp gen. IV", Log.getStackTraceString(e));
        } if  (e instanceof InvalidAlgorithmParameterException){
            Log.e("CT: invalid IV", Log.getStackTraceString(e));
        }
    }

    /**
     * Check an exception received when manipulating the keystore
     * @param e Exception to be checked
     */
    private static void checkExceptions2(Exception e){
        if (e instanceof NoSuchAlgorithmException) {
            Log.e("CT: ks integ. unchk.", Log.getStackTraceString(e));
        } if (e instanceof CertificateException) {
            Log.e("CT: ks certif. unloaded", Log.getStackTraceString(e));
        } if (e instanceof IOException) {
            Log.e("CT: ks wrong password", Log.getStackTraceString(e));
        } if (e instanceof KeyStoreException) {
            Log.e("CT: no provider type", Log.getStackTraceString(e));
        }
    }
}