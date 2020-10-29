package com.boruminc.borumjot.android;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONObject;

public class LabelActivity extends AppCompatActivity {
    /* Views */
    View appBar;

    /* Custom Objects */
    AppBarFragment appBarFrag;
    Label currentLabel;

    /* Primitives and Strings */
    String userApiKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);

        appBarFrag = ((AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar));
        appBar = findViewById(R.id.appbar);

        if (appBarFrag != null && getIntent().hasExtra("label")) {
            currentLabel = (Label) getIntent().getSerializableExtra("label");
            assert currentLabel != null;
            appBarFrag.passTitle(currentLabel.getName());
            appBar.setOnLongClickListener(this::onRenameLabel);
            userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");
        } else {
            finish();
            Toast.makeText(this, "The label screen is malfunctioning", Toast.LENGTH_LONG).show();
        }
    }

    private ApiRequestExecutor updateLabelRequest(String name) {
        return new ApiRequestExecutor(String.valueOf(currentLabel.getId()), name) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("PUT");
                setQuery(encodePostQuery("id=%s&name=%s"));
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeUrl("label"));
            }
        };
    }

    private boolean onRenameLabel(View view) {
        AlertDialog.Builder renameLabelBuilder = new AlertDialog.Builder(this);
        View renameJottingDialog = getLayoutInflater().inflate(R.layout.rename_jotting_dialog, null);

        TextView renameField = renameJottingDialog.findViewById(R.id.jot_name_edit);
        renameField.setText(currentLabel.getName());

        renameLabelBuilder
                .setTitle("Rename a Label")
                .setView(renameJottingDialog)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String newText = renameField.getText().toString();
                    if (newText.equals(currentLabel.getName()))
                        return;

                    new TaskRunner().executeAsync(updateLabelRequest(newText), data -> {
                        if (data != null && data.optInt("statusCode") >= 200 && data.optInt("statusCode") < 300) {
                            appBarFrag.passTitle(newText);
                        } else {
                            Toast.makeText(this, "The label was not renamed due to a system error", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

        renameLabelBuilder.create().show();
        return true;
    }
}
