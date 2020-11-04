package com.boruminc.borumjot.android;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONException;
import org.json.JSONObject;

public class ShareActivity extends FragmentActivity {
    String userApiKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);

        AppBarFragment shareAppBar = ((AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.share_appbar));
        if (shareAppBar != null) shareAppBar.passTitle("Share");

        userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private ApiRequestExecutor getShareRequest(int id, String email) {
        return new ApiRequestExecutor(String.valueOf(id), email) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("POST");
                setQuery(encodePostQuery("id=%s&email=%s"));
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString("note/share"));
            }
        };
    }

    private ApiResponseExecutor getShareResponse() {
        return new ApiResponseExecutor() {
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);
                if (ranOk()) {
                    finish();
                    Toast.makeText(getApplicationContext(), "Note successfully shared!", Toast.LENGTH_SHORT).show();
                } else if (result == null) {
                    Toast.makeText(getApplicationContext(), "The note could not be shared because of an unknown server error", Toast.LENGTH_LONG).show();
                } else {
                    String message = "";
                    try {
                        message = "The note could not be shared because " + result.getJSONObject("error").getString("message").toLowerCase();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        message = "The note could not be shared for an unknown reason";
                    } finally {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    public void onShareClick(View view) {
        EditText emailView = findViewById(R.id.share_email_field);
        new TaskRunner().executeAsync(
                getShareRequest(
                        getIntent().getIntExtra("id", 0),
                        emailView.getText().toString()
                ),
                getShareResponse()
        );
    }
}
