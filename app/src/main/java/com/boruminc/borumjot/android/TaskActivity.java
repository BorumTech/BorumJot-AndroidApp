package com.boruminc.borumjot.android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.customviews.EditTextV2;
import com.boruminc.borumjot.android.customviews.XButton;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.boruminc.borumjot.android.server.requests.DeleteJottingRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class TaskActivity extends FragmentActivity {
    private String userApiKey;
    private Task taskData;

    /* Views */
    private AppNameAppBarFragment appBarFrag;
    private EditText taskDescriptionBox;
    private CheckBox taskCompletionBox;
    private TableLayout subtaskList;
    private EditTextV2 newSubtaskField;

    /* Overriding Callback Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);

        taskDescriptionBox = findViewById(R.id.task_description_box);
        appBarFrag = (AppNameAppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        taskCompletionBox = findViewById(R.id.complete_task_btn);
        subtaskList = findViewById(R.id.task_subtasks_box);

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
            setTaskStatus(taskData.isCompleted());
            loadSubtasks(); // Set subtasks asynchronously
        }

        taskDescriptionBox.setOnFocusChangeListener(this::onDetailsBoxFocus);

        Spinner priorityDropdown = findViewById(R.id.task_priority_drpdwn);
        priorityDropdown.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        new String[] {"Top", "Mid", "Low"}
                )
        );
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /* Helper Methods */

    private void loadSubtasks() {
        new TaskRunner().executeAsync(
                new ApiRequestExecutor() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        setRequestMethod("GET");
                        addRequestHeader("Authorization", "Basic " + userApiKey);
                    }

                    @Override
                    public JSONObject call() {
                        super.call();
                        return this.connectToApi(encodeUrl("subtasks", "id=" + taskData.getId()));
                    }
                }, data -> {
                    try {
                        if (data != null && data.has("data") && data.getInt("statusCode") == 200) {
                            ArrayList<Task> subtaskData = JSONToModel.convertJSONToTasks(data.getJSONArray("data"));
                            setSubtasks(subtaskData);
                            taskData.setSubtasks(subtaskData);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "The subtasks could not load", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    /**
     * Displays the rename and name dialog in an <code>AlertDialog.Builder</code>
     */
    private void displayRenameDialog() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AlertDialog.Builder renameBuilder = new AlertDialog.Builder(this);
            renameBuilder
                    .setView(getLayoutInflater().inflate(R.layout.rename_jotting_dialog, null))
                    .setTitle("Task Name")
                    .setCancelable(true)
                    .setOnCancelListener(dialog -> finish())
                    .setPositiveButton("Save", (dialog, which) -> {
                        TextView titleTextView = ((Dialog) dialog).findViewById(R.id.jot_name_edit);

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

    /* Getter and Setter Methods */

    /**
     * Updates the UI of the appbar to display the passed in text
     * @param name The new name of the task
     */
    private void setTaskName(String name) {
        if (appBarFrag != null) appBarFrag.passTitle(name);
    }

    private String getTaskDetails() {
        return taskDescriptionBox.getText().toString();
    }

    private void setTaskDetails(String body) {
        taskDescriptionBox.setText(body);
    }

    private void setTaskStatus(boolean on) {
        appBarFrag.displayStrikethrough(on);
        taskCompletionBox.setChecked(on);
    }

    private void setSubtasks(ArrayList<Task> subtasks) {
        TableRow.LayoutParams subtaskTitleColumnLayoutParams = new TableRow.LayoutParams(800, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtaskTitleColumnLayoutParams.setMargins(10, 0, 10, 0);

        for (Iterator<Task> it = subtasks.iterator(); it.hasNext();) {
            Task subtask = it.next();
            Log.d("SUBTASK", subtask.toString());
            LinearLayout horizLayout = new TableRow(this);
            horizLayout.setTag(subtask.getId());
            horizLayout.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            EditTextV2 subtaskView = new EditTextV2(this);
            subtaskView.setText(subtask.getName());
            subtaskView.setTextSize(20f);
            subtaskView.setTextColor(Color.RED);
            subtaskView.setLayoutParams(subtaskTitleColumnLayoutParams);

            CheckBox checkBox = new CheckBox(this);
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            checkBox.setLayoutParams(layoutParams);

            ImageButton deleteBtn = new XButton(this);
            TableRow.LayoutParams deleteBtnLayoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            deleteBtnLayoutParams.gravity = Gravity.CENTER_VERTICAL;
            deleteBtn.setLayoutParams(deleteBtnLayoutParams);
            deleteBtn.setBackground(null);
            deleteBtn.setOnClickListener(this::onDeleteSubtaskClick);

            horizLayout.addView(checkBox);
            horizLayout.addView(subtaskView);
            horizLayout.addView(deleteBtn);

            subtaskList.addView(horizLayout);
        }

        TableRow addSubtaskLayout = new TableRow(this);
        addSubtaskLayout.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addSubtaskLayout.setGravity(Gravity.START);
        ImageButton addSubtaskBtn = new ImageButton(this);
        addSubtaskBtn.setBackgroundResource(R.drawable.floating_action_btn);
        addSubtaskBtn.setOnClickListener(this::onAddSubtaskClick);
        TableRow.LayoutParams addSubtaskBtnLayoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addSubtaskBtnLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        newSubtaskField = new EditTextV2(this);
        newSubtaskField.setLayoutParams(subtaskTitleColumnLayoutParams);
        addSubtaskLayout.addView(addSubtaskBtn);
        addSubtaskLayout.addView(newSubtaskField);
        subtaskList.addView(addSubtaskLayout);
    }

    /* Event Handlers */

    private void onDetailsBoxFocus(View view, boolean isFocused) {
        RelativeLayout.LayoutParams layoutParams;
        if (isFocused) {
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.extended_txtbox_height));

        } else {
            layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            if (!taskData.getBody().equals(getTaskDetails())) {
                new TaskRunner().executeAsync(
                        new ApiRequestExecutor(getTaskDetails()) {
                            @Override
                            protected void initialize() {
                                super.initialize();
                                setRequestMethod("PUT");
                                addRequestHeader("Authorization", "Basic " + userApiKey);
                                setQuery(encodePostQuery("body=%s"));
                            }

                            @Override
                            public JSONObject call() {
                                super.call();
                                return this.connectToApi(encodeUrl("task", "id=" + taskData.getId()));
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
                            new DeleteJottingRequest(taskData.getId(), userApiKey, "task"),
                            data -> {
                                try {
                                    if (data != null) {
                                        if (data.has("error") || data.getInt("statusCode") == 500) {
                                            Toast.makeText(this, "The task could not be deleted due to a system error", Toast.LENGTH_SHORT).show();
                                        } else {
                                            startActivity(new Intent(this, HomeActivity.class));
                                            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "The task could not be deleted due to a system error", Toast.LENGTH_LONG).show();
                                }
                            }
                    );

                })
                .setNegativeButton("Cancel", (dialog, which) -> {});
        deleteDialog.create().show();
    }

    public void onCompleteClick(View view) {
        int completed = ((CheckBox) view).isChecked() ? 1 : 0;
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
                        return this.connectToApi(encodeUrl("task", "completed=" + completed, "id=" + taskData.getId()));
                    }
                },
                data -> {
                    try {
                        if (data != null) {
                            if (data.has("error") || data.getInt("statusCode") != 200) {
                                Toast.makeText(this, "An error occurred and the task could not be marked as "
                                        + (completed == 1 ? "completed" : "incomplete"), Toast.LENGTH_LONG).show();
                            } else {
                                appBarFrag.displayStrikethrough(completed == 1);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "A server error occurred. The task was not updated", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void onAddSubtaskClick(View view) {
        if (newSubtaskField.getText() == null) return;

        new TaskRunner().executeAsync(
                new ApiRequestExecutor(String.valueOf(taskData.getId()), newSubtaskField.getText().toString()) {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        setRequestMethod("POST");
                        addRequestHeader("Authorization", "Basic " + userApiKey);
                        setQuery(encodePostQuery("id=%s&name=%s"));
                    }

                    @Override
                    public JSONObject call() {
                        super.call();
                        return this.connectToApi(this.encodeUrl("subtasks"));
                    }
                }, data -> {
                    if (data != null) {
                        try {
                            if (data.has("data") && data.getInt("statusCode") == 200) {
                                taskData.addSubtask(new Task("My new subtask name"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    public void onDeleteSubtaskClick(View view) {
        TableRow subtaskRow = (TableRow) view.getParent();

        new TaskRunner().executeAsync(
                new DeleteJottingRequest((Integer) subtaskRow.getTag(), userApiKey, "task"), data -> {
                if (data != null) {
                    if (data.optInt("statusCode") == 200) {
                        ((ViewGroup) subtaskRow.getParent()).removeView(subtaskRow);
                    }
                }
            }
        );
    }
}
