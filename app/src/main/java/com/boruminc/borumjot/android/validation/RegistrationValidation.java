package com.boruminc.borumjot.android.validation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.boruminc.borumjot.android.HomeActivity;
import com.boruminc.borumjot.android.R;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
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
                        new ApiRequestExecutor(firstName, lastName, getEmail(), getPassword()) {
                            protected void initialize() {
                                super.initialize();
                                setQuery(this.encodePostQuery("first_name=%s&last_name=%s&email=%s&password=%s"));
                                setRequestMethod("POST");
                            }

                            @Override
                            public JSONObject call() {
                                super.call();
                                return this.connectToApi(encodeQueryString("register", "app_api_key=9ds89d8as9das9"));
                            }
                        },
                        (data) -> {
                            progressBar.setVisibility(View.GONE); // Remove progress bar because the request is complete
                            try {
                                if (data != null) {
                                    if (data.isNull("error") && data.getInt("statusCode") == 200) {
                                        Intent homeIntent = new Intent(context, HomeActivity.class);

                                        // Set the API key in internal, private, app-specific storage (Shared Preferences)
                                        context.getSharedPreferences("user identification", Context.MODE_PRIVATE)
                                                .edit()
                                                .putString("apiKey", data.getJSONObject("data").getString("api_key"))
                                                .apply();

                                        context.startActivity(homeIntent);
                                    } else if ((data.getJSONObject("error")).has("message")) {
                                        Toast.makeText(context, (String) ((JSONObject) data.get("error")).get("message"), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, REGISTRATION_ERROR, Toast.LENGTH_LONG).show();
                            }
                        }
                );
    }

    /**
     * Validates the credentials the user entered for his or her new Borum account
     */
    public String validate() {
        if (isMissingFields()) return CREDENTIALS_NOT_COMPLETE;
        if (isEmailNotValid()) return INVALID_EMAIL;
        if (!isPasswordConfirmed()) return NO_PASSWORD_MATCH;

        progressBar.setVisibility(View.VISIBLE);
        return SUCCESS;
    }
}
