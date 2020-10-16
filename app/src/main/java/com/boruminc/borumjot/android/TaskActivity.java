package com.boruminc.borumjot.android;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;

import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.customviews.*;
import com.boruminc.borumjot.android.server.*;
import com.boruminc.borumjot.android.server.requests.DeleteJottingRequest;
import com.boruminc.borumjot.android.server.requests.UpdateTaskRequest;

import org.json.*;

import java.util.ArrayList;
import java.util.Iterator;

public class TaskActivity extends JottingActivity {
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
            setUserApiKey(getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", ""));
        }

        setJottingType("Task");
        if (getIntent().getBooleanExtra("Rename", false)) displayRenameDialog((dialog, which) -> {
            TextView titleTextView = ((Dialog) dialog).findViewById(R.id.jot_name_edit);

            if (titleTextView == null) { // Display error and exit if appbar title could not be found
                Toast.makeText(this, "An error occurred and you cannot name the task at this time. ", Toast.LENGTH_LONG).show();
                return;
            }

            setJottingName(titleTextView.getText().toString());
            new TaskRunner().executeAsync(new ApiRequestExecutor(titleTextView.getText().toString()) {
                @Override
                protected void initialize() {
                    super.initialize();
                    setQuery(this.encodePostQuery("name=%s"));
                    setRequestMethod("POST");

                    // Set the user's api key or an empty string in the Authentication request header
                    addRequestHeader("Authorization", "Basic " + getUserApiKey());
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
        else if (getIntent().hasExtra("data")) {
            setJottingData((Task) getIntent().getSerializableExtra("data"));
            assert getJottingData() != null;
            setJottingName(getJottingData().getName());
            setTaskDetails(getJottingData().getBody());
            setTaskStatus(getTaskData().isCompleted());
            loadSubtasks(); // Set subtasks asynchronously
        }

        taskDescriptionBox.setOnFocusChangeListener(this::onDetailsBoxFocus);
        findViewById(R.id.appbar).setOnLongClickListener(this::onRenameJotting);

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
                        addRequestHeader("Authorization", "Basic " + getUserApiKey());
                    }

                    @Override
                    public JSONObject call() {
                        super.call();
                        return this.connectToApi(encodeUrl("subtasks", "id=" + getJottingData().getId()));
                    }
                }, data -> {
                    try {
                        if (data != null && data.has("data") && data.getInt("statusCode") == 200) {
                            ArrayList<Task> subtaskData = JSONToModel.convertJSONToTasks(data.getJSONArray("data"));
                            setSubtasks(subtaskData);
                            getTaskData().setSubtasks(subtaskData);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "The subtasks could not load", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    /**
     * Calls {@link JottingActivity#displayRenameDialog(DialogInterface.OnClickListener)}
     * with "Task" as the second parameter
     */
    protected void displayRenameDialog(DialogInterface.OnClickListener onPositiveButtonClick) {
        super.displayRenameDialog(onPositiveButtonClick);
    }

    /* Getter and Setter Methods */

    /**
     * Implements {@link JottingActivity#setJottingName(String)}
     * @param name The new name of the jotting
     */
    protected void setJottingName(String name) {
        if (appBarFrag != null) appBarFrag.passTitle(name);
    }

    /**
     * Convenience method for getting the jotting data with the Task type
     * @return The jotting data as a Task object
     */
    private Task getTaskData() {
        return (Task) getJottingData();
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
        TableRow.LayoutParams subtaskTitleColumnLayoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtaskTitleColumnLayoutParams.setMargins(10, 0, 10, 0);

        for (Iterator<Task> it = subtasks.iterator(); it.hasNext();) {
            addSubtask(it.next(), subtaskList.getChildCount());
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
        newSubtaskField.setId(R.id.newSubtaskFieldId);

        addSubtaskLayout.addView(addSubtaskBtn);
        addSubtaskLayout.addView(newSubtaskField);
        subtaskList.addView(addSubtaskLayout);
    }

    private void addSubtask(Task subtask, int index) {
        TableRow.LayoutParams subtaskTitleColumnLayoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout horizLayout = new TableRow(this);
        horizLayout.setTag(subtask.getId());
        horizLayout.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditTextV2 subtaskView = new EditTextV2(this);
        subtaskView.setText(subtask.getName());
        subtaskView.setTextSize(20f);
        subtaskView.setTextColor(Color.RED);
        subtaskView.setLayoutParams(subtaskTitleColumnLayoutParams);
        subtaskView.setOnFocusChangeListener(this::onSubtaskBoxFocus);
        // Display strikethrough if the subtask is marked as complete
        if (subtask.isCompleted())
            subtaskView.setPaintFlags(subtaskView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        CheckBox checkBox = new CheckBox(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        checkBox.setLayoutParams(layoutParams);
        checkBox.setOnClickListener(this::onCompleteSubtaskClick);
        checkBox.setChecked(subtask.isCompleted());

        ImageButton deleteBtn = new XButton(this);
        TableRow.LayoutParams deleteBtnLayoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteBtnLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        deleteBtn.setLayoutParams(deleteBtnLayoutParams);
        deleteBtn.setBackground(null);
        deleteBtn.setOnClickListener(this::onDeleteSubtaskClick);

        horizLayout.addView(checkBox);
        horizLayout.addView(subtaskView);
        horizLayout.addView(deleteBtn);

        subtaskList.addView(horizLayout, index);
    }

    /* Event Handlers */

    private void onDetailsBoxFocus(View view, boolean isFocused) {
        LinearLayout.LayoutParams layoutParams;
        if (isFocused) {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.extended_txtbox_height));
        } else {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            if (!getJottingData().getBody().equals(getTaskDetails())) {
                new TaskRunner().executeAsync(
                        new UpdateTaskRequest(getUserApiKey(), new String[] {"id=" + getJottingData().getId()}, new String[] {getTaskDetails()}), data -> {
                            if (data != null) {
                                if (data.has("error"))
                                    Toast.makeText(this, "The tasks details could not be saved due to an error", Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }
        }
        view.setLayoutParams(layoutParams);
    }

    private void onSubtaskBoxFocus(View view, boolean isFocused) {
        ViewGroup subtaskRow = (TableRow) view.getParent();
        ViewGroup subtasksBox = (TableLayout) subtaskRow.getParent();

        int id = (int) subtaskRow.getTag();
        String contents = ((EditText) view).getText().toString();
        String originalContents = getTaskData().getSubtasks().get(subtasksBox.indexOfChild(subtaskRow)).getName();

        if (isFocused || contents.equals(originalContents)) return;

        new TaskRunner().executeAsync(new UpdateTaskRequest(getUserApiKey(), new String[] {"id=" + id, "name=" + contents}, null), data -> {
            if (data == null || data.has("error")) {
                Toast.makeText(this, "A system error occurred and the subtask could not update", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onDeleteClick(View view) {
        android.app.AlertDialog.Builder deleteDialog = new android.app.AlertDialog.Builder(this);
        deleteDialog
                .setTitle("Delete Task")
                .setMessage("Are you sure you would like to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new TaskRunner().executeAsync(
                            new DeleteJottingRequest(getTaskData().getId(), getUserApiKey(), "task"),
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
                        addRequestHeader("Authorization", "Basic " + getUserApiKey());
                    }

                    @Override
                    public JSONObject call() {
                        super.call();
                        return this.connectToApi(encodeUrl("task", "completed=" + completed, "id=" + getTaskData().getId()));
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
        // Exit early if the field unexpectedly doesn't exist
        if (newSubtaskField == null || newSubtaskField.getText() == null) return;

        // Exit early and display error when newSubtaskField is empty
        if (newSubtaskField.getText().toString().equals("")) {
            Toast.makeText(this, "The new subtask can't be empty", Toast.LENGTH_LONG).show();
            return;
        }

        new TaskRunner().executeAsync(
                new ApiRequestExecutor(String.valueOf(getTaskData().getId()), newSubtaskField.getText().toString()) {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        setRequestMethod("POST");
                        addRequestHeader("Authorization", "Basic " + getUserApiKey());
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
                            if (data.getInt("statusCode") == 200) {
                                Task subtask = new Task(newSubtaskField.getText().toString());
                                addSubtask(subtask, getTaskData().getSubtasks().size());
                                ((EditText) subtaskList.findViewById(R.id.newSubtaskFieldId)).setText("");
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
                new DeleteJottingRequest((Integer) subtaskRow.getTag(), getUserApiKey(), "task"), data -> {
                if (data != null) {
                    if (data.optInt("statusCode") == 200) {
                        ((ViewGroup) subtaskRow.getParent()).removeView(subtaskRow);
                    }
                }
            }
        );
    }

    public void onCompleteSubtaskClick(View view) {
        TableRow subtaskRow = (TableRow) view.getParent();
        int completed = ((CheckBox) view).isChecked() ? 1 : 0;
        new TaskRunner().executeAsync(
                new ApiRequestExecutor() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        setRequestMethod("PUT");
                        addRequestHeader("Authorization", "Basic " + getUserApiKey());
                    }

                    @Override
                    public JSONObject call() {
                        super.call();
                        return this.connectToApi(
                                encodeUrl(
                                        "task",
                                        "completed=" + completed,
                                        "id=" + subtaskRow.getTag()
                                )
                        );
                    }
                },
                data -> {
                    try {
                        if (data != null) {
                            if (data.has("error") || data.getInt("statusCode") != 200) {
                                Toast.makeText(this, "An error occurred and the task could not be marked as "
                                        + (completed == 1 ? "complete" : "incomplete"), Toast.LENGTH_LONG).show();
                            } else {
                                EditText subtaskTxt = (EditText) subtaskRow.getChildAt(1);
                                // TODO Move into new class/fragment? because same code as in AppNameAppBarFragment
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    if (completed == 1)
                                        // Add a strikethrough to the already existing paint flags using "|" bitwise operator
                                        subtaskTxt.setPaintFlags(subtaskTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                    else
                                        // Remove the strikethrough from the paint flags using "&" bitwise operator
                                        subtaskTxt.setPaintFlags(subtaskTxt.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "A server error occurred. The task was not updated", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
