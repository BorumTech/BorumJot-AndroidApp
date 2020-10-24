package com.boruminc.borumjot.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AesEncryptionTest {

    @Test
    public void instantiation_isPossible() {
        assertTrue(new AesEncryption("") instanceof AesEncryption);
    }
}
