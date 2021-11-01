package com.boruminc.borumjot.android.validation;

import android.app.Activity;

import com.boruminc.borumjot.android.validation.RegistrationValidation;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegisterValidationTest {
    @Mock
    Activity mContext;

    /**
     * Tests if registration validates correctly whether the the password and confirm password fields have the same text
     */
    @Test
    @Ignore("Throws error")
    public void registerMatchingPasswordValidation_isCorrect() {
        RegistrationValidation registrationValidation = new RegistrationValidation(mContext, "", "", "", "pass", "pass");
        boolean resultWithMatch = registrationValidation.isPasswordConfirmed();
        assertTrue(resultWithMatch);
    }

    @Ignore("Throws error")
    @Test
    public void nonMatchingPasswordValidation_isCorrect() {
        RegistrationValidation registrationValidation = new RegistrationValidation(mContext, "", "", "", "pass1", "pass2");
        boolean resultWithoutMatch = registrationValidation.isPasswordConfirmed();
        assertFalse(resultWithoutMatch);
    }
}
