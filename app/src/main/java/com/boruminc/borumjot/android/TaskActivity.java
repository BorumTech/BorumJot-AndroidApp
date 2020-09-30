package com.boruminc.borumjot.android;

import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class TaskActivity extends FragmentActivity {
    private String userApiKey;
    private Task taskData;

    /* Views */
    private EditText taskDescriptionBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);

        taskDescriptionBox = findViewById(R.id.task_description_box);

        // Set the userApiKey for class use
        if (getSharedPreferences("user identification", Context.MODE_PRIVATE) != null) {
            userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");
        }

        if (getIntent().getBooleanExtra("Rename", false)) displayRenameDialog();
        else if (getIntent().hasExtra("data")) {
            taskData = (Task) getIntent().getSerializableExtra("data");
            assert taskData != null;
            setTaskName(taskData.getName());
            setTaskDetails(taskData.getBody());
        }

        taskDescriptionBox.setOnFocusChangeListener(this::onDetailsBoxFocus);
    }

    private void displayRenameDialog() {
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

                        setTaskName(titleTextView.getText().toString());
                        new TaskRunner().executeAsync(new ApiRequestExecutor(titleTextView.getText().toString()) {
                            @Override
                            protected void initialize() {
                                super.initialize();
                                setQuery(this.encodePostQuery("name=%s"));
                                setRequestMethod("POST");

                                // Set the user's api key or an empty string in the Authentication request header
                                addRequestHeader("Authorization", "Basic " + userApiKey);
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

    private String getTaskName() {
        return ((TextView) findViewById(R.id.appbar_title)).getText().toString();
    }

    /**
     * Updates the UI of the appbar to display the passed in text
     * @param name The new name of the task
     */
    private void setTaskName(String name) {
        AppNameAppBarFragment frag = (AppNameAppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        if (frag != null) frag.passTitle(name);
    }

    private String getTaskDetails() {
        return taskDescriptionBox.getText().toString();
    }

    private void setTaskDetails(String body) {
        taskDescriptionBox.setText(body);
    }

    private void onDetailsBoxFocus(View view, boolean isFocused) {
        RelativeLayout.LayoutParams layoutParams;
        if (isFocused) {
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.extended_txtbox_height));

        } else {
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            if (!taskData.getBody().equals(getTaskDetails())) {
                new TaskRunner().executeAsync(
                        new ApiRequestExecutor() {
                            @Override
                            protected void initialize() {
                                super.initialize();
                                setRequestMethod("PUT");
                                addRequestHeader("Authorization", "Basic " + userApiKey);
                            }

                            @Override
                            public JSONObject call() {
                                super.call();
                                return this.connectToApi(
                                        encodeUrl("task",
                                                "body=" + getTaskDetails(),
                                                "id=" + taskData.getId()
                                        )
                                );
                            }
                        }, data -> {
                            if (data != null) {
                                if (data.has("error"))
                                    Toast.makeText(this, "The tasks details could not be saved due to an error", Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }
        }
        layoutParams.addRule(RelativeLayout.BELOW, R.id.header_btns);
        view.setLayoutParams(layoutParams);
    }

    public void onDeleteClick(View view) {
        android.app.AlertDialog.Builder deleteDialog = new android.app.AlertDialog.Builder(this);
        deleteDialog
                .setTitle("Delete Task")
                .setMessage("Are you sure you would like to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new TaskRunner().executeAsync(
                            new ApiRequestExecutor((String.valueOf(taskData.getId()))) {
                                @Override
                                protected void initialize() {
                                    super.initialize();
                                    setRequestMethod("DELETE");
                                    addRequestHeader("Authorization", "Basic " + userApiKey);
                                    setQuery(encodePostQuery("id=%s"));
                                }

                                @Override
                                public JSONObject call() {
                                    super.call();
                                    return this.connectToApi(encodeUrl("task"));
                                }
                            },
                            data -> {
                                try {
                                    if (data != null) {
                                        if (data.has("error") || data.getInt("statusCode") == 500) {
                                            Toast.makeText(this, "The task could not be deleted due to a system error", Toast.LENGTH_SHORT).show();
                                        } else {
                                            startActivity(new Intent(this, HomeActivity.class));
                                            Toast.makeText(this, "The task was deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "The task could not be deleted due to a system error", Toast.LENGTH_LONG).show();
                                }
                            }
                    );

                })
                .setNegativeButton("Cancel", (dialog, which) -> {

                });
        deleteDialog.create().show();
    }
}
