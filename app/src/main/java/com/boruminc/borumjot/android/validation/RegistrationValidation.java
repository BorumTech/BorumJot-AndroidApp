package com.boruminc.borumjot.android.validation;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.boruminc.borumjot.android.HomeActivity;
import com.boruminc.borumjot.android.R;
import com.boruminc.borumjot.android.server.RegisterUser;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationValidation extends Validation {
    private String firstName;
    private String lastName;
    private String confirmPassword;
    private ProgressBar progressBar;

    private static final String REGISTRATION_ERROR = "Registration failed: An error occurred";
    private static final String NO_PASSWORD_MATCH = "Registration failed: Passwords don't match";
    private static final String CREDENTIALS_NOT_COMPLETE = "Registration failed: Not all fields were filled out";

    /**
     * Constructor for RegistrationValidation object
     * @param context The activity that contains the loading circle and registration form
     * @param fn The first name of the user
     * @param ln The last name of the user
     * @param em The email that would be associated with the user's new Borum account
     * @param pw The password that would be associated with the user's new Borum account
     * @param confPw The string the user entered into the confirm password field
     */
    public RegistrationValidation(Activity context, String fn, String ln, String em, String pw, String confPw) {
        super(em, pw);
        firstName = fn;
        lastName = ln;
        confirmPassword = confPw;
        progressBar = context.findViewById(R.id.progressPanel);
    }

    public boolean isPasswordConfirmed() {
        return getPassword().equals(confirmPassword);
    }

    @Override
    public boolean isMissingFields() {
        return super.isMissingFields() || firstName.isEmpty() || lastName.isEmpty() || confirmPassword.isEmpty();
    }

    public void checkRegistration(Activity context) {
        new TaskRunner()
                .executeAsync(
                        new RegisterUser(firstName, lastName, getEmail(), getPassword()),
                        (data) -> {
                            progressBar.setVisibility(View.GONE);
                            try {
                                if (data != null) {
                                    if (data.isNull("error") && data.getInt("statusCode") == 200) {
                                        context.startActivity(new Intent(context, HomeActivity.class));
                                    } else if ((data.getJSONObject("error")).has("message")) {
                                        Toast.makeText(context, (String) ((JSONObject) data.get("error")).get("message"), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Registration failed: An error occurred", Toast.LENGTH_LONG).show();
                            }
                        }
                );
    }

    /**
     * Validates the credentials the user entered for his or her new Borum account
     */
    public String validate() {
        if (isMissingFields()) return CREDENTIALS_NOT_COMPLETE;
        if (!isEmailValid()) return INVALID_EMAIL;
        if (!isPasswordConfirmed()) return NO_PASSWORD_MATCH;

        progressBar.setVisibility(View.VISIBLE);
        return SUCCESS;
    }
}
