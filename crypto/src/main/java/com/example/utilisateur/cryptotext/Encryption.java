package com.example.utilisateur.cryptotext;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by DonatienTERTRAIS on 10/09/2016.
 */

//The keys are stocked in a file, this file is protected with a password which is to be given every time a key is used (before openning and creating a conversation



public class Encryption {
    private static final String keyStoreFile = "/storage/emulated/CryptoText/keys.keystore";
    private static final Level level = Level.WARNING;
    protected static Logger logger = Logger.getLogger(Encryption.class.getName());

    public static KeyStore createKeyStore(String filePassword) {
        File file = new File(keyStoreFile);

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("JCEKS");
        } catch (KeyStoreException e) {
            logger.log(level, e.toString());
        }
        if (file.exists()) {
            // .keystore file already exists => load it
            try {
                keyStore.load(new FileInputStream(file), filePassword.toCharArray());
            } catch ( NoSuchAlgorithmException e) {
                logger.log(level, e.toString());// No algorithm to check the keystore integrity
            } catch ( CertificateException e) {
                logger.log(level, e.toString());// KeyStore certificates cannot be loaded
            } catch ( IOException e) {
                logger.log(level, e.toString());// Error keystore file password
            }
        } else {
            // .keystore file not created yet => create it
            try {
                keyStore.load(null, null);
                keyStore.store(new FileOutputStream(keyStoreFile), filePassword.toCharArray());
            } catch ( NoSuchAlgorithmException e) {
                logger.log(level, e.toString());// The algorithm to check the integrity cannot be found
            } catch ( CertificateException e) {
                logger.log(level, e.toString());// The certificate cannot be stored
            } catch ( IOException e) {
                logger.log(level, e.toString());
            } catch ( KeyStoreException e) {
                logger.log(level, e.toString());// KeyStore not loaded
            }
        }
        return keyStore;
    }

    // Encrypts string and encodes in Base64
    public static String encrypt( String password, String data ) throws Exception {
        byte[] secretKey = password.getBytes();
        byte[] clear = data.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec( secretKey, "AES" );
        Cipher cipher = Cipher.getInstance("AES" );
        cipher.init( Cipher.ENCRYPT_MODE, secretKeySpec );

        byte[] encrypted = cipher.doFinal(clear);
        String encryptedString = Base64.encodeToString(encrypted, Base64.DEFAULT);

        return encryptedString;
    }

    // Decrypts string encoded in Base64
    public static String decrypt( String password, String encryptedData ) throws Exception {
        byte[] secretKey = password.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec( secretKey, "AES" );
        Cipher cipher = Cipher.getInstance( "AES" );
        cipher.init( Cipher.DECRYPT_MODE, secretKeySpec );

        byte[] encrypted = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted);
    }

    public static SecretKey generateKey( byte[] seed ) {
        KeyGenerator keyGenerator;
        SecureRandom secureRandom;
        try {
            keyGenerator = KeyGenerator.getInstance( "AES" );
            secureRandom = SecureRandom.getInstance( "SHA1PRNG" );
            secureRandom.setSeed( seed );
            keyGenerator.init( 128, secureRandom );
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            logger.log(level, e.toString());// Unknown algorithm for key/random number generation
            return null;
        }
    }

    public static boolean isStocked(String entry, String filePassword) {
        KeyStore keyStore = createKeyStore(filePassword);
        boolean cond = false;
        try {
            cond = keyStore.containsAlias(entry);
        } catch (KeyStoreException e) {
            logger.log(level, e.toString());
        }
        return cond;
    }
}
