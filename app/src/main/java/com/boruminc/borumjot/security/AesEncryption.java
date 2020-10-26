package com.boruminc.borumjot.security;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class for encrypting text using the AES algorithm
 */
@RequiresApi(api = Build.VERSION_CODES.O)
class AesEncryption {
    private String plaintext;

    AesEncryption(String pt) {
        plaintext = pt;
    }

    String getPlaintext() {
        return plaintext;
    }

    @NonNull
    public String toString() {
        return "Plaintext: " + plaintext;
    }

    /**
     * Encrypts the plaintext using the secret key and initialization vector
     * @param secretKey The secret key used to encrypt (and decrypt)
     * @param initVector The randomness added at the beginning as a byte[]
     * @return The cipher text
     * @throws GeneralSecurityException if something unexpected occurs
     */
    String encrypt(SecretKey secretKey, byte[] initVector) throws GeneralSecurityException {
        // Get Cipher instance that will use AES encryption algorithm in CBC mode with a PKCS5Padding padding scheme
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Create the SecretKeySpec using the passed in secret key
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

        // Create IvParameterSpec, a wrapper for the initialization vector
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector);

        // Initialize the cipher instance with the key and algorithm parameters
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] cipherText = cipher.doFinal(getPlaintext().getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }
}
