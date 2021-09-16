package com.boruminc.borumjot.android.labels;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.boruminc.borumjot.android.R;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class NewLabelActivity extends Activity {

    private String userApiKey;

    private TextInputEditText labelName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_label);

        // Set the userApiKey for use throughout the class
        if (getSharedPreferences("user identification", Context.MODE_PRIVATE) != null) {
            userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");
        }

        labelName = findViewById(R.id.label_name);
    }

    public void onExitCreateLabelClick(View view) {
        finish();
    }

    public void onConfirmLabelClick(View view) {
        String newLabelName = Objects.requireNonNull(labelName.getText()).toString();
        new TaskRunner().executeAsync(createNewLabel(newLabelName), handleNewLabelResponse());
    }

    /**
     * Returns the requests that creates a new label to be added to any jotting
     *
     * @param newLabelName The name of the new label
     * @return The ApiRequestExecutor object
     */
    protected ApiRequestExecutor createNewLabel(String newLabelName) {
        return new ApiRequestExecutor(newLabelName) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("POST");
                addAuthorizationHeader(userApiKey);
                setQuery(encodePostQuery("name=%s"));
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString("label"));
            }
        };
    }

    private ApiResponseExecutor handleNewLabelResponse() {
        return new ApiResponseExecutor() {
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);
                try {
                    if (ranOk()) {
                        Intent confirmNewLabel = new Intent(NewLabelActivity.this, LabelActivity.class);
                        confirmNewLabel.putExtra("label", JSONToModel.convertJSONToLabel(result.getJSONObject("data")));
                        startActivity(confirmNewLabel);
                    } else {
                        Toast.makeText(NewLabelActivity.this, "A server error occurred", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
