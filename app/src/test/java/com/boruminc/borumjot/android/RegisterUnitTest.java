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
        RegistrationValidation registrationValidation = new RegistrationValidation("", "", "", "pass", "pass");
        boolean resultWithMatch = registrationValidation.isPasswordConfirmed();
        assertTrue(resultWithMatch);
    }

    @Test
    public void nonMatchingPasswordValidation_isCorrect() {
        RegistrationValidation registrationValidation = new RegistrationValidation("", "", "", "pass1", "pass2");
        boolean resultWithoutMatch = registrationValidation.isPasswordConfirmed();
        assertFalse(resultWithoutMatch);
    }
}
