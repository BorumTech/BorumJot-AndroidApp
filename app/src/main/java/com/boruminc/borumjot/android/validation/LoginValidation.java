package com.boruminc.borumjot.android.validation;

import com.boruminc.borumjot.android.server.LoginUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public final class LoginValidation extends Validation {

    private static final String LOGIN_ERROR = "Login failed: An error occurred";
    private static final String CREDENTIALS_NOT_COMPLETE = "Login failed: Not all fields were filled out";

    /**
     * Checks if the email and password are in the same row in the database using Borum Jot API
     * @param em The email entered by the user
     * @param pw The password entered by the user
     */
    public LoginValidation(String em, String pw) {
        super(em, pw);
    }

    /**
     * Check if credentials are valid
     * @return Whether the credentials are valid:
     * true if valid, false if invalid or an error occurred
     */
    private String checkLogin() {
        try {
            JSONObject execution = new LoginUser().execute(getEmail(), getPassword()).get();
            if (execution != null) {
                if (execution.has("error"))
                    return ((JSONObject) execution.get("error")).getString("message");
                else return SUCCESS;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return LOGIN_ERROR;
    }

    /**
     * Validates a user's login credentials
     * @return A string indicating whether the validation was successful, and if not all errors
     */
    public String validate() {
        if (isMissingFields()) return CREDENTIALS_NOT_COMPLETE;
        if (!isEmailValid()) return INVALID_EMAIL;

        return checkLogin();
    }
}
