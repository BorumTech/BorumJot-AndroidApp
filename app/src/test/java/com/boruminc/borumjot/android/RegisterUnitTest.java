package com.boruminc.borumjot.android;

import com.boruminc.borumjot.android.validation.LoginValidation;
import com.boruminc.borumjot.android.validation.RegistrationValidation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegisterUnitTest {
    /**
     * Tests if registration validates correctly whether the the password and confirm password fields have the same text
     */
    @Test
    public void registerMatchingPasswordValidation_isCorrect() {
        boolean resultWithMatch = RegistrationValidation.isPasswordConfirmed("pass", "pass");
        assertTrue(resultWithMatch);

        boolean resultWithoutMatch = RegistrationValidation.isPasswordConfirmed( "pass1", "pass2");
        assertFalse(resultWithoutMatch);

    }
}
