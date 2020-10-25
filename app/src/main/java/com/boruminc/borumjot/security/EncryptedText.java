package com.boruminc.borumjot.security;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Class for encrypting text, automatically generating a secret key and initialization vector
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class EncryptedText {
    /**
     * Encrypts text
     * @param plainText The text to be encrypted
     * @return The cipher text
     */
    static String encryptText(String plainText) {
        try {
            AesEncryption encryption = new AesEncryption(plainText);
            return encryption.encrypt(generateSecretKey(), generateInitializationVector());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Generates a 128 bit secret key
     * @return The SecretKey object
     * @throws NoSuchAlgorithmException if the algorithm associated with the key doesn't exist
     */
    static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey(); // Generate key
    }

    static byte[] generateInitializationVector() {
        byte[] initVector = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(initVector);

        return initVector;
    }
}
