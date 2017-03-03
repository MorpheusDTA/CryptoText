package com.example.utilisateur.cryptotext;

import android.content.Context;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Level level = Level.WARNING;
    private static final String ENCRYPTION_IV = "4e5Wa71fYoT7MFEX";
    private static Logger logger = Logger.getLogger(Encryption.class.getName());
    private static final String KEY_FILE_NAME = "CryptoKeys.keystore";

    /**
     * Creates a KeyStore file or loads it if was already created
     * @param filePassword Password of the KeyStore file
     * @return The KeyStore file
     */
    static KeyStore createKeyStore(Context context, String filePassword) {
        File file = new File(context.getFilesDir(), KEY_FILE_NAME);

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            logger.log(level, "No provider supports the specified type: " + e.toString());
        }
        if (file.exists()) {
            // .keystore file already exists => load it
            try {
                keyStore.load(new FileInputStream(file), filePassword.toCharArray());

            } catch ( NoSuchAlgorithmException e) {
                logger.log(level, "No algorithm to check the keystore integrity:" + e.toString());
            } catch ( CertificateException e) {
                logger.log(level, "KeyStore Certificates cannot be loaded: " + e.toString());
            } catch ( IOException e) {
                keyStore = null;
                logger.log(level, "Error on keyStore file password: " + e.toString());
            }
        } else {
            // .keystore file not created yet => create it
            try {
                keyStore.load(null, null);
                keyStore.store(new FileOutputStream(KEY_FILE_NAME), filePassword.toCharArray());
            } catch ( NoSuchAlgorithmException e) {
                logger.log(level, "The algorithm to check the integrity cannot be found: " + e.toString());
            } catch ( CertificateException e) {
                logger.log(level, "The certificate cannot be stored: " + e.toString());
            } catch ( IOException e) {
                logger.log(level, "I/O Problem with data to be stored: " + e.toString());
            } catch ( KeyStoreException e) {
                logger.log(level, "KeyStore not loaded: " + e.toString());
            }
        }
        return keyStore;
    }

    /**
     * This function test a password on a keystore
     * @param password Password that is to be tried
     * @return True if the password is correct, false otherwise
     */
    static boolean testPassword(Context context, String password) {
        File file = new File(context.getFilesDir(), KEY_FILE_NAME);
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("JCEKS");
        } catch (KeyStoreException e) {
            logger.log(level, "No provider supports the specified type: " + e.toString());
        }
        if (file.exists()) {
            // .keystore file already exists => load it
            try {
                keyStore.load(new FileInputStream(file), password.toCharArray());

            } catch ( Exception e) {
                logger.log(level, "Exception on testing password:" + e.toString());
                return false;
            }
        }
        return true;
    }

    /**
     *Checks if the keystore file exists
     * @return True if the file exists, false otherwise
     */
    static boolean exists(Context context) {
        File file = new File(context.getFilesDir(), KEY_FILE_NAME);
        return (file.exists());
    }

    /**
     * Encrypts data and encodes it into Base64
     * @param key Key to be used
     * @param data data to be encrypted
     * @return Encrypted Data
     */
    static String encrypt( String key, String data ) {
        byte[] encrypted = null;
        IvParameterSpec iv = null;
        try {
            iv = new IvParameterSpec(ENCRYPTION_IV.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.log(level, "Impossible to generate IV: " + e.toString());
        }
        byte[] clear = data.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec( key.getBytes(), "AES" );
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

            encrypted = cipher.doFinal(clear);
        } catch (Exception e) {
            checkExceptions (e);
        }
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
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
            checkExceptions (e);
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
            logger.log(level, "Unknown algorithm for key/random number generation: " + e.toString());
            return null;
        }
    }

    /**
     * Checked if a given entry is used to stock a key
     * @param entry Entry to be checked
     * @param filePassword Password of the KeyStore file
     * @return Whether the entry is stocked or not
     */
    static boolean isStocked(Context context, String entry, String filePassword) {
        KeyStore keyStore = createKeyStore(context, filePassword);
        boolean cond = false;
        try {
            cond = keyStore.containsAlias(entry);
        } catch (KeyStoreException e) {
            logger.log(level, "KeyStore not loaded/initialized: " + e.toString());
        }
        return cond;
    }

    /**
     * Get a key from an alias
     * @param alias Alias of the searched key
     * @param filePassword Password of the KeyStore file
     * @return The searched key
     */
    static String getKey(Context context, String alias, String filePassword) {
        String key = "";
        KeyStore keyStore = createKeyStore(context, filePassword);
        if (keyStore == null) {
            return null;
        }
        try {
            key = keyStore.getKey(alias, filePassword.toCharArray()).toString();
        } catch (KeyStoreException e) {
            logger.log(level, "KeyStore is not loaded/initialized: " + e.toString());
        } catch (NoSuchAlgorithmException e) {
            logger.log(level, "No algorithm to find the key: " + e.toString());
        } catch (UnrecoverableKeyException e) {
            logger.log(level, "Key cannot be recovered (wrong password): " + e.toString());
        }
        return key;
    }

    /**
     * Saves a key from a seed
     * @param seed Seed to be used
     * @param filePassword Password of the Keystore file
     * @param alias Alias to be given to the Key
     */
    static void saveKey(Context context, String seed, String filePassword, String alias) {
        KeyStore keyStore = createKeyStore(context, filePassword);
        if (keyStore == null) {
            return;
        }
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(filePassword.toCharArray());
        try {
            if (seed.length() != 0) {
                KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(Encryption.generateKey(seed.getBytes()));
                keyStore.setEntry(alias, keyStoreEntry, passwordProtection);
            }
        } catch (KeyStoreException e) {
            logger.log(level, "KeyStore is not loaded/initialized: " + e.toString());
        }
    }

    /**
     * Check an exception received when encrypting or decrypting data
     * @param e Exception to be checked
     */
    private static void checkExceptions(Exception e){
        if (e instanceof NoSuchAlgorithmException) {
            logger.log(level, "Transformation invalid or no provider supports it: " + e.toString());
        } if (e instanceof NoSuchPaddingException) {
            logger.log(level, "The transformation contains a non-available pagging scheme: " + e.toString());
        } if (e instanceof InvalidKeyException) {
            logger.log(level, "The Cipher could not be initialized to key parameter: " + e.toString());
        } if (e instanceof IllegalBlockSizeException) {
            logger.log(level, "Error on BlockSize, you may verify padding scheme" + e.toString());
        } if (e instanceof BadPaddingException) {
            logger.log(level, "Decrypted data is not bounded by the appropriate padding bytes: " + e.toString());
        } if  (e instanceof UnsupportedEncodingException) {
            logger.log(level, "Impossible to generate IV: " + e.toString());
        } if  (e instanceof InvalidAlgorithmParameterException){
            logger.log(level, "Invalid Iv: " + e.toString());
        }
    }
}