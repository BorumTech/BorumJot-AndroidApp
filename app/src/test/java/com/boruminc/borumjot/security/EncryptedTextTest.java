package com.boruminc.borumjot.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.apache.commons.codec.binary.Base64;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class EncryptedTextTest {
    @Test
    public void encryption_outputsBase64String() {
        String cipherText = EncryptedText.encryptText("Hallelujah");
        assertTrue(Base64.isBase64(cipherText));
    }

    @Test
    public void secretKey_is128Bits() {
        try {
            SecretKey secretKey = EncryptedText.generateSecretKey();
            int secretKeyBits = secretKey.getEncoded().length * 8;
            assertEquals(128, secretKeyBits);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void initializationVector_isRandom() {
        assertNotEquals(EncryptedText.generateInitializationVector(), EncryptedText.generateInitializationVector());
    }
}
