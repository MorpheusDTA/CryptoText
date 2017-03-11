package com.example.utilisateur.cryptotext;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
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
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

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
 * This class contains the main methods dealing with key nd keystore management
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
    static String encrypt(SecretKeySpec key, String data ) {
        try {
            IvParameterSpec iv = new IvParameterSpec(ENCRYPTION_IV.getBytes("UTF-8"));
            byte[] clear = data.getBytes();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
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
    static String decrypt(SecretKeySpec key, String encryptedData ) {
        IvParameterSpec iv;
        byte[] decrypted = new byte[]{};
        try {
            iv = new IvParameterSpec(ENCRYPTION_IV.getBytes("UTF-8"));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init( Cipher.DECRYPT_MODE, key, iv);

            byte[] encrypted = Base64.decode(encryptedData, Base64.DEFAULT);
            decrypted = cipher.doFinal(encrypted);
        } catch (Exception e) {
            checkExceptions1 (e);
        }
        return new String(decrypted);
    }

    /**
     * Generate a key from a given seed
     * @return Key generated
     */
    static SecretKeySpec generateKey() {
        // Generate a 256-bit key
        final int outputKeyLength = 256;
        SecureRandom secureRandom = new SecureRandom();
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(outputKeyLength, secureRandom);
            SecretKeySpec key = (SecretKeySpec) keyGenerator.generateKey();
            return key;
        } catch (NoSuchAlgorithmException e) {
            Log.e("CT: cannot gen key", Log.getStackTraceString(e));
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
    static SecretKeySpec getKey(Context context, String alias, String pwd) {
        KeyStore keyStore = getKeyStore(context, pwd);

        if (keyStore == null) return null;

        try {
            return (SecretKeySpec) keyStore.getKey(alias, pwd.toCharArray());
        } catch (KeyStoreException e) {
            Log.e("CT: ks not init", Log.getStackTraceString(e));
            return null;
        } catch (NoSuchAlgorithmException e) {
            Log.e("CT: no algo find key", Log.getStackTraceString(e));
            return null;
        } catch (UnrecoverableKeyException e) {
            Log.e("CT: key unrecover.", Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Saves a key
     * @param context The context of the application
     * @param key Key to be saved
     * @param pwd Password of the Keystore file
     * @param alias Alias to be given to the Key
     */
    static void saveKey(Context context, SecretKey key, String pwd, String alias) {
        KeyStore keyStore = getKeyStore(context, pwd);
        if (keyStore == null) return;
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(pwd.toCharArray());
        try {
            SecretKeyEntry keyStoreEntry = new SecretKeyEntry(key);
            keyStore.setEntry(alias, keyStoreEntry, passwordProtection);
            saveKeyStore(context, keyStore, pwd);
        } catch (KeyStoreException e) {
            Log.e("CT: ks not init.", Log.getStackTraceString(e));
        }
    }

    /**
     * Delete a key that is to be overwritten
     * @param context Contextof the application
     * @param alias Key's alias in the keystore
     * @param pwd Password of the keystore
     */
    static void deleteKey(Context context, String alias, String pwd) {
        try {
            KeyStore keyStore = getKeyStore(context, pwd);
            keyStore.deleteEntry(alias);
            saveKeyStore(context, keyStore, pwd);
        } catch (KeyStoreException e) {
            Log.e("CT: cannot delete key", Log.getStackTraceString(e));
        }
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

    /**
     * Gets all keys stored in the keyStore
     * @param context Context of the app
     * @param pwd Password of the KeyStore
     * @return List of the aliases and the string representations of the corresponding key
     */
    static ArrayList<String> getKeys(Context context, String pwd) {
        KeyStore keyStore = getKeyStore(context, pwd);
        if (keyStore == null) return null;
        ArrayList<String> list = new ArrayList<>();
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Key key = keyStore.getKey(alias, pwd.toCharArray());

                // Get the contact name
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(alias));
                Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        alias = alias + "/" + cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                    cursor.close();
                }

                if (key != null) {
                    String keyStr = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
                    list.add(alias + "\n" + keyStr);
                }
            }
        } catch (KeyStoreException e) {
            Log.e("CT: cannot get aliases", Log.getStackTraceString(e));
        } catch (NoSuchAlgorithmException e) {
            Log.e("CT: cannot get a key", Log.getStackTraceString(e));
        } catch (UnrecoverableKeyException e) {
            Log.e("CT: cannot recover key", Log.getStackTraceString(e));
        }
        return list;
    }

    /**
     * Changes the password of the keystore file
     * @param context Context of the app
     * @param oldPwd Former password
     * @param newPwd New password
     * @return If the change was successful
     */
    static boolean changePwd(Context context, String oldPwd, String newPwd) {
        KeyStore keyStore = getKeyStore(context, oldPwd);
        if (keyStore == null) return false;
        HashMap<String, SecretKeyEntry> hashMap = new HashMap<>();//map of the aliases-secret keys
        ArrayList<String> aliasList = new ArrayList<>();//list of the aliases
        try {
            KeyStore.PasswordProtection newPwdProtec = new KeyStore.PasswordProtection(newPwd.toCharArray());
            KeyStore.PasswordProtection oldPwdProtec = new KeyStore.PasswordProtection(oldPwd.toCharArray());
            Enumeration<String> aliases = keyStore.aliases();

            while (aliases.hasMoreElements()) {// Stock the keys and aliases in the hashMap
                String alias = aliases.nextElement();
                aliasList.add(alias);
                SecretKeyEntry key = (SecretKeyEntry) keyStore.getEntry(alias, oldPwdProtec);
                if (key != null) {
                    hashMap.put(alias, key);
                }
            }

            saveKeyStore(context, keyStore, oldPwd);
            context.deleteFile(KEY_FILE_NAME);// delete keystore and recreate it with the new password
            exists(context);
            createKeyStore(context, newPwd);

            KeyStore keyStore2 = getKeyStore(context, newPwd);//Reload the keystore, save the key and save the keystore
            for (String alias : aliasList) {
                SecretKeyEntry key = hashMap.get(alias);
                keyStore2.setEntry(alias, key, newPwdProtec);
            }

            saveKeyStore(context, keyStore2, newPwd);
            return true;
        } catch (KeyStoreException e) {
            Log.e("CT: cannot get aliases", Log.getStackTraceString(e));
        } catch (NoSuchAlgorithmException e) {
            Log.e("CT: cannot get a key", Log.getStackTraceString(e));
        } catch (UnrecoverableKeyException e) {
            Log.e("CT: cannot recover key", Log.getStackTraceString(e));
        } catch (UnrecoverableEntryException e) {
            Log.e("CT: cant recover entry", Log.getStackTraceString(e));
        }
        return false;
    }
}