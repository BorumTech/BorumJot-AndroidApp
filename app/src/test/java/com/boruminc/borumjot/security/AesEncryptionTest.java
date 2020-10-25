package com.boruminc.borumjot.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class AesEncryptionTest {
    private String plaintext;

    public AesEncryptionTest(String p) {
        super();
        plaintext = p;
    }

    @Parameterized.Parameters
    public static Collection input() {
        return Arrays.asList(new String[][]{{"Hello world"}, {"Goodbye world"}, {"Bar"}, {"Foo"}});
    }

    @Test
    public void instantiation_isPossible() {
        assertTrue(new AesEncryption("") instanceof AesEncryption);
    }

    @Test
    public void plaintextGetter_isCorrect() {
        assertEquals(plaintext, new AesEncryption(plaintext).getPlaintext());
    }
}
