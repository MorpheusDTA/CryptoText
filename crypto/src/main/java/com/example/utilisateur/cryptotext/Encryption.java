package com.example.utilisateur.cryptotext;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author DonatienTERTRAIS
 */

//The keys are stocked in a file, this file is protected with a password which is to be given every time a key is used (before openning and creating a conversation



class Encryption {
    private static final String keyStoreFile = "/storage/emulated/CryptoText/keys.keystore";
    private static final Level level = Level.WARNING;
    private static Logger logger = Logger.getLogger(Encryption.class.getName());

    static KeyStore createKeyStore(String filePassword) {
        File file = new File(keyStoreFile);

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("JCEKS");
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
                keyStore.store(new FileOutputStream(keyStoreFile), filePassword.toCharArray());
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

    // Encrypts string and encodes in Base64
    public static String encrypt( String seed, String data ) throws Exception {
        byte[] clear = data.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec( seed.getBytes(), "AES" );
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init( Cipher.ENCRYPT_MODE, secretKeySpec );

        byte[] encrypted = cipher.doFinal(clear);

        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    // Decrypts string encoded in Base64
    static String decrypt( String seed, String encryptedData ) {
        SecretKeySpec secretKeySpec = new SecretKeySpec( seed.getBytes(), "AES" );
        byte[] decrypted = new byte[]{};
        try {
            Cipher cipher = Cipher.getInstance( "AES" );
            cipher.init( Cipher.DECRYPT_MODE, secretKeySpec );

            byte[] encrypted = Base64.decode(encryptedData, Base64.DEFAULT);
            decrypted = cipher.doFinal(encrypted);
        } catch(NoSuchAlgorithmException e) {
            logger.log(level, "Transformation invalid or no provider supports it: " + e.toString());
        } catch(NoSuchPaddingException e) {
            logger.log(level, "The transformation contains a non-available pagging scheme: " + e.toString());
        } catch(InvalidKeyException e) {
            logger.log(level, "The Cipher could not be initialized to key parameter: " + e.toString());
        }catch(IllegalBlockSizeException e) {
            logger.log(level, "Error on BlockSize, you may verify padding scheme" + e.toString());
        }catch(BadPaddingException e) {
            logger.log(level, "Decrypted data is not bounded by the appropriate padding bytes: " + e.toString());
        }

        return new String(decrypted);
    }

    static SecretKey generateKey( byte[] seed ) {
        KeyGenerator keyGenerator;
        SecureRandom secureRandom;
        try {
            keyGenerator = KeyGenerator.getInstance( "AES" );
            secureRandom = SecureRandom.getInstance( "SHA1PRNG" );
            secureRandom.setSeed( seed );
            keyGenerator.init( 128, secureRandom );
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            logger.log(level, "Unknown algorithm for key/random number generation: " + e.toString());
            return null;
        }
    }

    public static boolean isStocked(String entry, String filePassword) {
        KeyStore keyStore = createKeyStore(filePassword);
        boolean cond = false;
        try {
            cond = keyStore.containsAlias(entry);
        } catch (KeyStoreException e) {
            logger.log(level, "KeyStore not loaded/initialized: " + e.toString());
        }
        return cond;
    }
}
