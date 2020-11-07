package com.boruminc.borumjot.android;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONObject;

public class ChangeSignInActivity extends FragmentActivity {
    AppBarFragment appBarFragment;

    private String userApiKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_sign_in_activity);

        userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");

        displayAppBar();
        setEmailDefaultText();
    }

    private void displayAppBar() {
        appBarFragment = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        if (appBarFragment != null) {
            appBarFragment.passTitle("Change Borum Sign In");
        }
    }

    /**
     * Sets the text for the email EditText with the user's current email
     */
    private void setEmailDefaultText() {
        String email = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("email", "");
        if (email.equals("")) {
            finish();
            Toast.makeText(this, "You must be logged in to change your sign in", Toast.LENGTH_SHORT).show();
        }
        ((TextView) findViewById(R.id.email_field)).setText(email);
    }

    public void onChangeSignInClick(View view) {
        new TaskRunner().executeAsync(getChangeSignInRequest(), handleChangeSignInResponse());
    }

    private ApiRequestExecutor getChangeSignInRequest() {
        return new ApiRequestExecutor(
                ApiRequestExecutor.RequestUrl.BORUM_REQUEST_URL,
                ((TextView) findViewById(R.id.password_field)).getText().toString()
        ) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("PUT");
                setQuery("new_password=%s");
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString("login"));
            }
        };
    }

    private ApiResponseExecutor handleChangeSignInResponse() {
        return new ApiResponseExecutor() {
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);
                if (result != null && result.optInt("statusCode") == 200) {
                    finish();
                    Toast.makeText(
                            ChangeSignInActivity.this,
                            "Sign-in successfully updated",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(ChangeSignInActivity.this, "An error occurred and the sign-in could not be updated", Toast.LENGTH_LONG).show();
                }
            }
        };
    }
}