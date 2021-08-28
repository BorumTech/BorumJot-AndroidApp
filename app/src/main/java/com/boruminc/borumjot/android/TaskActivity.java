package com.boruminc.borumjot.android;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.NotificationCompat;

import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.customviews.EditTextV2;
import com.boruminc.borumjot.android.customviews.XButton;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.SlashNormalizer;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.boruminc.borumjot.android.server.requests.DeleteJottingRequest;
import com.boruminc.borumjot.android.server.requests.UpdateTaskRequest;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Locale;

public class TaskActivity extends JottingActivity {
    /* Views */
    private AppBarFragment appBarFrag;
    private EditText taskDescriptionBox;
    private CheckBox taskCompletionBox;
    private TableLayout subtaskList;
    private EditTextV2 newSubtaskField;
    private FlexboxLayout labelsList;

    private Intent nextIntent;

    /* Overriding Callback Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);

        taskDescriptionBox = findViewById(R.id.task_description_box);
        appBarFrag = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        taskCompletionBox = findViewById(R.id.complete_task_btn);
        subtaskList = findViewById(R.id.task_subtasks_box);
        labelsList = findViewById(R.id.task_labels_box);

        // Set the userApiKey for class use
        if (getSharedPreferences("user identification", Context.MODE_PRIVATE) != null) {
            setUserApiKey(getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", ""));
        }

        setJottingType("Task");
        if (getIntent().getBooleanExtra("Rename", false)) displayRenameDialog((dialog, which) -> {
            setJottingData(new Task());
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
                    return this.connectToApi(encodeQueryString("task"));
                }
            }, data -> {
                try {
                    if (data != null) {
                        if (data.has("error") && data.getJSONObject("error").has("message")) {
                            Toast.makeText(TaskActivity.this, "The task could not be created at this time. ",
                                    Toast.LENGTH_SHORT).show();
                        } else if (data.optInt("statusCode") >= 200 && data.optInt("statusCode") < 300) {
                            getJottingData().setName(titleTextView.getText().toString());
                            getJottingData().setId(data.getJSONObject("data").getInt("id"));
                            new TaskRunner().executeAsync(getJottingLabels(), this::loadLabels);
                            setDueDate(null);
                            handleDueDates();
                            setTaskStatus(false);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        });
        else if (getIntent().hasExtra("data")) {
            setJottingData((Task) getIntent().getSerializableExtra("data"));
            assert getJottingData() != null;
            setJottingName(getJottingData().getName());
            setTaskDetails(getJottingData().getBody());
            setTaskStatus(getTaskData().isCompleted());
            setDueDate(getTaskData().getDueDate());
            loadSubtasks(); // Set subtasks asynchronously
        }

        taskDescriptionBox.setOnFocusChangeListener(this::onDetailsBoxFocus);
        findViewById(R.id.appbar).setOnLongClickListener(this::onRenameJotting);

        if (getJottingData() != null) {
            new TaskRunner().executeAsync(getJottingLabels(), this::loadLabels);
            handleDueDates();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showLabelImageBtn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nextIntent == null)
            overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        nextIntent = null;
    }

    /* Helper Methods */

    private void showLabelImageBtn() {
        ImageButton labelControlsBtn = new ImageButton(this);
        labelControlsBtn.setBackgroundResource(R.drawable.label_black_outline);
        FlexboxLayout.LayoutParams controlsLayoutParams = new FlexboxLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.label_controls_btn_width),
                (int) getResources().getDimension(R.dimen.label_btn_height)
        );

        if (labelsList.getChildAt(0) != null && labelsList.getChildAt(0).getLayoutParams().width == controlsLayoutParams.width) return;

        controlsLayoutParams.setMargins(20, 0, 15, 0);
        labelControlsBtn.setLayoutParams(controlsLayoutParams);
        labelControlsBtn.setOnClickListener(this::onLabelControlsBtnClick);
        labelsList.addView(labelControlsBtn, 0);
    }

    private void setSubtask(EditText view, boolean isCompleted) {
        if (isCompleted)
            // Add a strikethrough to the already existing paint flags using "|" bitwise operator
            view.setPaintFlags(view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            view.setPaintFlags(view.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    /**
     * Loads the subtasks.
     */
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
                        return this.connectToApi(encodeQueryString("subtasks", "id=" + getJottingData().getId()));
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

    private void handleDueDates() {
        DatePickerDialog.OnDateSetListener onDueDateSetListener = (view, year, month, dayOfMonth) -> {
            Date chosenDueDate = Date.valueOf(year + "-" + (month + 1) + "-" + dayOfMonth);

            ApiRequestExecutor updateDueDateRequest = new ApiRequestExecutor() {
                @Override
                protected void initialize() {
                    super.initialize();
                    setRequestMethod("PUT");
                    addAuthorizationHeader(getUserApiKey());
                }

                @Override
                public JSONObject call() {
                    super.call();
                    return this.connectToApi(encodeQueryString(
                            "task",
                            "id=" + getTaskData().getId(),
                            "due_date=" + (chosenDueDate.getTime() / 1000)));
                }
            };

            ApiResponseExecutor updateDueDateResponse = new ApiResponseExecutor() {
                @Override
                public void onComplete(JSONObject result) {
                    super.onComplete(result);
                    try {
                        if (ranOk()) {
                            Log.d("Due Date after update", String.valueOf(chosenDueDate.getTime()));
                            setDueDate(chosenDueDate);
                            getTaskData().setDueDate(chosenDueDate);
                        } else if (result.has("error") && result.getJSONObject("error").has("message")) {
                            String errorMessage = result.getJSONObject("error").getString("message");
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            new TaskRunner().executeAsync(updateDueDateRequest, updateDueDateResponse); // Update due date

            // Schedule a notification for all tasks whose due date are set in the future
            java.util.Date currentDate = new java.util.Date();
            if (chosenDueDate.getTime() >= currentDate.getTime()) {
                long delay = chosenDueDate.getTime() - currentDate.getTime();
                delay -= 24 * 3600 * 1000; // Clock back a day
                scheduleTaskDueDateNotification(delay);
            }
        };

        View.OnClickListener onDueDateBtnClick = v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Date dueDate = getTaskData().getDueDate();
                LocalDate localDate = dueDate != null ?
                        dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() :
                        LocalDate.now();

                int month = localDate.getMonthValue();
                int dayOfMonth = localDate.getDayOfMonth();
                int year = localDate.getYear();

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        onDueDateSetListener,
                        year,
                        month - 1,
                        dayOfMonth
                );
                datePickerDialog.setOnDateSetListener(onDueDateSetListener);
                datePickerDialog.show();
            } else {
                Toast.makeText(this, "Your phone is too old to support due dates. Try on the web app", Toast.LENGTH_LONG).show();
            }
        };

        findViewById(R.id.due_date_btn).setOnClickListener(onDueDateBtnClick);
        setDueDate(getTaskData().getDueDate());
    }

    /**
     * Calls {@link JottingActivity#displayRenameDialog(DialogInterface.OnClickListener)}
     * with "Task" as the second parameter
     */
    protected void displayRenameDialog(DialogInterface.OnClickListener onPositiveButtonClick) {
        super.displayRenameDialog(onPositiveButtonClick);
    }

    private ImageButton generateCherryRasboraPlusButton() {
        ImageButton imageBtn = new ImageButton(this);
        imageBtn.setBackgroundResource(R.drawable.floating_action_btn);
        return imageBtn;
    }

    /**
     * Sends notification at 1200 hours GMT the day before the set due date
     */
    private void scheduleTaskDueDateNotification(long delay) {
        final String PACKAGE_NAME = "com.boruminc.borumjot.android";
        final String CATEGORY_NAME = "Tasks";
        final int NOTIFICATION_ID = 5 * getTaskData().getId() + getTaskData().getUserId() % 4;

        Intent notificationIntent = new Intent(getApplicationContext(), TaskNotificationPublisher.class);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Reminder for " + getTaskData().getName())
                .setContentText("\"" + getTaskData().getName() + "\" is due tomorrow")
                .setAutoCancel(true)
                .setCategory(CATEGORY_NAME)
                .setChannelId(PACKAGE_NAME);

        Notification notification = notificationBuilder.build();

        notificationIntent.putExtra("notification", notification);
        notificationIntent.putExtra("notification_id", NOTIFICATION_ID);

        // Use Intent with same info as current intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        notificationBuilder.setContentIntent(pendingIntent);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        }
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
     * Updates the UI for labels
     * @param labels The new labels list
     */
    protected void setLabels(ArrayList<Label> labels) {
        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.label_btn_width),
                (int) getResources().getDimension(R.dimen.label_btn_height)
        );
        layoutParams.setMargins(20, 0, 15, 0);

        for (int i = 0; i < labels.size(); i++) {
            Button labelButton = new Button(this);
            labelButton.setText(labels.get(i).getName());
            labelButton.setLayoutParams(layoutParams);
            labelButton.setOnClickListener(this::onLabelClick);

            labelsList.addView(labelButton);
        }
    }

    /**
     * Convenience method for getting the jotting data with the Task type
     * @return The jotting data as a Task object
     */
    private Task getTaskData() {
        return (Task) getJottingData();
    }

    /**
     * Gets the UI's task details
     * @return The currently displayed text of the description box
     */
    private String getTaskDetails() {
        return taskDescriptionBox.getText().toString();
    }

    /**
     * Sets the task details in the UI
     * @param body The new body of the task
     */
    private void setTaskDetails(String body) {
        taskDescriptionBox.setText(SlashNormalizer.unescapeUserSlashes(body));
    }

    private void setTaskStatus(boolean on) {
        appBarFrag.displayStrikethrough(on);
        taskCompletionBox.setChecked(on);
    }

    private void setSubtasks(ArrayList<Task> subtasks) {
        TableRow.LayoutParams subtaskTitleColumnLayoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtaskTitleColumnLayoutParams.setMargins(10, 0, 10, 0);

        subtaskList.setColumnShrinkable(2, true);

        for (Task subtask : subtasks) {
            subtaskList.addView(addSubtask(subtask, subtaskList.getChildCount()));
        }

        TableRow addSubtaskLayout = new TableRow(this);
        addSubtaskLayout.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addSubtaskLayout.setGravity(Gravity.START);
        ImageButton addSubtaskBtn = generateCherryRasboraPlusButton();
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

    private void setDueDate(Date dueDate) {
        TextView dueDateView = findViewById(R.id.due_date);
        if (dueDate != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
            String formattedDate = simpleDateFormat.format(dueDate);
            dueDateView.setText(formattedDate);
        } else {
            dueDateView.setText(R.string.no_due_date);
        }
    }

    private LinearLayout addSubtask(Task subtask, int index) {
        LinearLayout horizLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.subtask, null);
        horizLayout.setTag(subtask.getId());

        TextView title = horizLayout.findViewById(R.id.subtask_title);
        title.setText(subtask.getName());
        title.setOnFocusChangeListener(this::onSubtaskBoxFocus);
        // Display strikethrough if the subtask is marked as complete
        if (subtask.isCompleted())
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        CheckBox checkBox = horizLayout.findViewById(R.id.completed_status);
        checkBox.setChecked(subtask.isCompleted());

        return horizLayout;
    }

    /* Event Handlers */

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
                        return this.connectToApi(encodeQueryString("task", "completed=" + completed, "id=" + getTaskData().getId()));
                    }
                },
                new ApiResponseExecutor() {
                    @Override
                    public void onComplete(JSONObject data) {
                        super.onComplete(data);
                        if (ranOk()) {
                            appBarFrag.displayStrikethrough(completed == 1);
                        } else {
                            Toast.makeText(getApplicationContext(), "An error occurred and the task could not be marked as "
                                + (completed == 1 ? "completed" : "incomplete"), Toast.LENGTH_LONG).show();
                        }
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
                        return this.connectToApi(this.encodeQueryString("subtasks"));
                    }
                }, data -> {
                    if (data != null) {
                        try {
                            if (data.getInt("statusCode") >= 200 && data.getInt("statusCode") < 300) {
                                Task subtask = new Task(newSubtaskField.getText().toString());
                                subtask.setId(data.getJSONObject("data").getInt("id"));
                                getTaskData().getSubtasks().add(subtask);
                                addSubtask(subtask, getTaskData().getSubtasks().size() - 1);
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
                                encodeQueryString(
                                        "task",
                                        "completed=" + completed,
                                        "id=" + subtaskRow.getTag()
                                )
                        );
                    }
                },
                new ApiResponseExecutor() {
                    @Override
                    public void onComplete(JSONObject result) {
                        super.onComplete(result);
                        if (ranOk()) {
                            EditText subtaskTxt = (EditText) subtaskRow.getChildAt(1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                if (completed == 1)
                                    setSubtask(subtaskTxt, true);
                                else
                                    setSubtask(subtaskTxt, false);
                            }
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "An error occurred and the task could not be marked as " + (completed == 1 ? "complete" : "incomplete"),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
        );
    }

    private void onDetailsBoxFocus(View view, boolean isFocused) {
        if (!getJottingData().getBody().equals(getTaskDetails())) {
            new TaskRunner().executeAsync(
                new UpdateTaskRequest(getUserApiKey(), new String[] {"id=" + getJottingData().getId()}, new String[] {getTaskDetails()}), data -> {
                    if (data != null) {
                        if (data.has("error"))
                            Toast.makeText(this, "The tasks details could not be saved due to an error", Toast.LENGTH_LONG).show();
                        else if (data.optInt("statusCode") >= 200 && data.optInt("statusCode") < 300) {
                            getJottingData().setBody(getTaskDetails());
                        }
                    }
                }
            );
        }
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

    private void onLabelClick(View view) {
        Intent labelAct = new Intent(this, LabelActivity.class);
        labelAct.putExtra("label", getJottingData().getLabels().get(labelsList.indexOfChild(view) - 1));
        startActivity(labelAct);
    }

    public void navigateToShare(View view) {
        nextIntent = new Intent(this, ShareActivity.class);
        nextIntent.putExtra("jotting", getTaskData());
        nextIntent.putExtra("jotType", "task");
        startActivity(nextIntent);
    }
}
