package com.boruminc.borumjot.android;

import android.content.Context;

import com.boruminc.borumjot.android.validation.LoginValidation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LoginUnitTest {
    @Mock
    Context mMockContext;

    /**
     * Tests if login credential validation is correct
     */
    @Test
    public void loginCredentialValidation_isCorrect() {
        boolean resultWithInvalidCredentials= LoginValidation.isCredentialsValid("armageddon@gmail.com","pass");
        assertTrue(resultWithInvalidCredentials);

        boolean resultWithValidCredentials = LoginValidation.isCredentialsValid("armageddon@gmail.com", "pass123!");
        assertFalse(resultWithValidCredentials);
    }

    /**
     * Tests if login email validation is correct
     */
    @Test
    public void loginEmailValidation_isCorrect() {
        boolean resultWithValidEmail = LoginValidation.isEmailValid("arigergage@gmail.com");
        assertTrue(resultWithValidEmail);

        boolean resultWithInvalidEmail = LoginValidation.isEmailValid("arifsid");
        assertFalse(resultWithInvalidEmail);
    }


}
