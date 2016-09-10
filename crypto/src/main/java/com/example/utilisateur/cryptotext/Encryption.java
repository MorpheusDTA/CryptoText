package com.example.utilisateur.cryptotext;

import android.util.Base64;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by DonatienTERTRAIS on 10/09/2016.
 */
public class Encryption {

    // Encrypts string and encodes in Base64
    public static String encrypt( String password, String data ) throws Exception
    {
        byte[] secretKey = generateKey( password.getBytes() );
        byte[] clear = data.getBytes();

        SecretKeySpec secretKeySpec = new SecretKeySpec( secretKey, "AES" );
        Cipher cipher = Cipher.getInstance("AES" );
        cipher.init( Cipher.ENCRYPT_MODE, secretKeySpec );

        byte[] encrypted = cipher.doFinal( clear );
        String encryptedString = Base64.encodeToString(encrypted, Base64.DEFAULT);

        return encryptedString;
    }

    // Decrypts string encoded in Base64
    public static String decrypt( String password, String encryptedData ) throws Exception
    {
        byte[] secretKey = generateKey(password.getBytes());
        SecretKeySpec secretKeySpec = new SecretKeySpec( secretKey, "AES" );
        Cipher cipher = Cipher.getInstance( "AES" );
        cipher.init( Cipher.DECRYPT_MODE, secretKeySpec );

        byte[] encrypted = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted);
    }

    public static byte[] generateKey( byte[] seed ) throws Exception
    {
        KeyGenerator keyGenerator = KeyGenerator.getInstance( "AES" );
        SecureRandom secureRandom = SecureRandom.getInstance( "SHA1PRNG" );
        secureRandom.setSeed( seed );
        keyGenerator.init( 128, secureRandom );
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }
}
