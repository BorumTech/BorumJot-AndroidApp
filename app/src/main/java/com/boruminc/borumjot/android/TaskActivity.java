package com.boruminc.borumjot.android;

import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class TaskActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getBooleanExtra("Rename", false)) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AlertDialog.Builder renameBuilder = new AlertDialog.Builder(this);
                renameBuilder
                        .setView(getLayoutInflater().inflate(R.layout.rename_task_dialog, null))
                        .setTitle("Task Name")
                        .setCancelable(true)
                        .setOnCancelListener(dialog -> finish())
                        .setPositiveButton("Save", (dialog, which) -> {
                            TextView titleTextView = ((Dialog) dialog).findViewById(R.id.task_name_edit);

                            if (titleTextView == null) { // Display error and exit if appbar title could not be found
                                Toast.makeText(this, "An error occurred and you cannot name the task at this time. ", Toast.LENGTH_LONG).show();
                                return;
                            }

                            AppNameAppBarFragment frag = (AppNameAppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
                            if (frag != null) frag.passTitle(titleTextView.getText().toString());
                            new TaskRunner().executeAsync(new ApiRequestExecutor(titleTextView.getText().toString()) {
                                @Override
                                protected void initialize() {
                                    super.initialize();
                                    setQuery(this.encodePostQuery("name=%s"));
                                    setRequestMethod("POST");

                                    // Set the user's api key or an empty string in the Authentication request header
                                    String apiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");
                                    addRequestHeader("Authorization", "Basic " + apiKey);
                                }

                                @Override
                                public JSONObject call() {
                                    super.call();
                                    return this.connectToApi(encodeUrl("task"));
                                }
                            }, data -> {
                                try {
                                    if (data != null) {
                                        if (data.has("error") && data.getJSONObject("error").has("message")) {
                                            Toast.makeText(TaskActivity.this, "The task could not be created at this time. ",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        });
                renameBuilder.create().show();
            } else {
                Toast.makeText(this, "Your phone is too old to name or rename the task. You can only rename on the website", Toast.LENGTH_LONG).show();
            }


        }

    }
}
