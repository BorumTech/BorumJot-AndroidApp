package com.boruminc.borumjot.android;

import android.app.Activity;
import android.content.Context;

import com.boruminc.borumjot.android.validation.LoginValidation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@RunWith(MockitoJUnitRunner.class)
public class LoginUnitTest {
    @Mock
    Activity mMockContext;

    /**
     * Tests if login credential validation is correct for valid login credentials
     */

    public void loginCredentialValidation_isCorrect() {
        LoginValidation loginValidation = new LoginValidation(mMockContext, "armageddon@gmail.com","pass");
        String resultWithInvalidCredentials = loginValidation.validate();
        assertEquals(resultWithInvalidCredentials, LoginValidation.SUCCESS);
    }

    /**
     * Tests if login credential validation is correct for invalid login credentials
     */

    public void invalidLoginCredentialValidation_isCorrect() {
        LoginValidation loginValidation = new LoginValidation(mMockContext, "armageddon@gmail.com", "pass123!");
        String resultWithValidCredentials = loginValidation.validate();
        assertNotEquals(resultWithValidCredentials, LoginValidation.SUCCESS);
    }

    /**
     * Tests if login email validation is correct for valid emails
     * by asserting that a valid email returns true
     */
    @Test
    public void loginEmailValidation_isCorrect() {
        LoginValidation loginValidation = new LoginValidation(mMockContext, "arigergage@gmail.com", "");
        boolean resultWithValidEmail = loginValidation.isEmailValid();
        assertTrue(resultWithValidEmail);
    }

    /**
     * Tests if login email validation is correct for invalid emails
     * by asserting that an invalid email returns false
     */
    @Test
    public void invalidEmailValidation_isCorrect() {
        LoginValidation loginValidation = new LoginValidation(mMockContext, "arifsid", "");
        boolean resultWithInvalidEmail = loginValidation.isEmailValid();
        assertFalse(resultWithInvalidEmail);
    }


}
