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

public final class LoginValidation extends Validation {
    private Activity context;
    private ProgressBar progressBar;

    private static final String LOGIN_ERROR = "Login failed: An error occurred";
    private static final String CREDENTIALS_NOT_COMPLETE = "Login failed: Not all fields were filled out";

    /**
     * Checks if the email and password are in the same row in the database using Borum Jot API
     * @param em The email entered by the user
     * @param pw The password entered by the user
     */
    public LoginValidation(Activity context, String em, String pw) {
        super(em, pw);
        this.context = context;
        progressBar = context.findViewById(R.id.progressPanel);
    }

    /**
     */
    public void checkLogin() {
        new TaskRunner()
                .executeAsync(
                        new ApiRequestExecutor(getEmail(), getPassword()) {
                            protected void initialize() {
                                super.initialize();
                                setQuery(this.encodePostQuery("email=%s&password=%s"));
                                setRequestMethod("POST");
                            }
                            @Override
                            public JSONObject call() {
                                super.call();
                                return this.connectToApi(encodeUrl("login", "app_api_key=9ds89d8as9das9"));
                            }
                        },
                        (data) -> {
                            progressBar.setVisibility(View.GONE); // Remove progress bar to indicate request is complete
                            try {
                                if (data != null) {
                                    if (data.isNull("error") && data.getInt("statusCode") == 200) {
                                        Intent homeIntent = new Intent(context, HomeActivity.class);
                                        String userApiKey = data.getJSONObject("data").getString("api_key");

                                        // Set the API key in internal, private, app-specific storage (Shared Preferences)
                                        context.getSharedPreferences("user identification", Context.MODE_PRIVATE)
                                                .edit()
                                                .putString("apiKey", userApiKey)
                                                .apply();

                                        context.startActivity(homeIntent);
                                    } else if (data.getJSONObject("error").has("message")) {
                                        Toast.makeText(context, (String) ((JSONObject) data.get("error")).get("message"), Toast.LENGTH_LONG).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, LOGIN_ERROR, Toast.LENGTH_LONG).show();
                            }
                        }
                );
    }

    /**
     * Validates a user's login credentials
     * @return A string indicating whether the validation was successful, and if not all errors
     */
    public String validate() {
        if (isMissingFields()) return CREDENTIALS_NOT_COMPLETE;
        if (!isEmailValid()) return INVALID_EMAIL;

        progressBar.setVisibility(View.VISIBLE);
        return SUCCESS;
    }
}
