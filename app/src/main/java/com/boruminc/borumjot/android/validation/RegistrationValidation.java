package com.boruminc.borumjot.android.validation;

import com.boruminc.borumjot.android.server.RegisterUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class RegistrationValidation extends Validation {
    private String firstName;
    private String lastName;
    private String confirmPassword;

    public static RegisterUser registerTask;

    private static final String REGISTRATION_ERROR = "Registration failed: An error occurred";
    private static final String NO_PASSWORD_MATCH = "Registration failed: Passwords don't match";
    private static final String CREDENTIALS_NOT_COMPLETE = "Registration failed: Not all fields were filled out";

    /**
     * Constructor for RegistrationValidation object
     * @param fn The first name of the user
     * @param ln The last name of the user
     * @param em The email that would be associated with the user's new Borum account
     * @param pw The password that would be associated with the user's new Borum account
     * @param confPw The string the user entered into the confirm password field
     */
    public RegistrationValidation(String fn, String ln, String em, String pw, String confPw) {
        super(em, pw);
        firstName = fn;
        lastName = ln;
        confirmPassword = confPw;
    }

    public boolean isPasswordConfirmed() {
        return getPassword().equals(confirmPassword);
    }

    @Override
    public boolean isMissingFields() {
        return super.isMissingFields() || firstName.isEmpty() || lastName.isEmpty() || confirmPassword.isEmpty();
    }

    private String checkRegistration() {
        try {
            registerTask = new RegisterUser();
            JSONObject execution = registerTask.execute(firstName, lastName, getEmail(), getPassword()).get();
            if (execution != null) {
                if (execution.isNull("error")) {
                    return SUCCESS;
                } else if (!((JSONObject) execution.get("error")).isNull("message")) {
                    return (String) ((JSONObject) execution.get("error")).get("message");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return REGISTRATION_ERROR;
    }

    /**
     * Validates the credentials the user entered for his or her new Borum account
     */
    public String validate() {
        if (!isEmailValid()) return INVALID_EMAIL;
        if (!isPasswordConfirmed()) return NO_PASSWORD_MATCH;
        if (isMissingFields()) return CREDENTIALS_NOT_COMPLETE;
        return checkRegistration();
    }
}
