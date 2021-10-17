package com.boruminc.borumjot.android.subtasks;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.HomeActivity;
import com.boruminc.borumjot.android.JottingActivity;
import com.boruminc.borumjot.android.R;
import com.boruminc.borumjot.android.TaskActivity;
import com.boruminc.borumjot.android.TaskNotificationPublisher;
import com.boruminc.borumjot.android.customviews.EditTextV2;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.SlashNormalizer;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.boruminc.borumjot.android.server.requests.DeleteJottingRequest;
import com.boruminc.borumjot.android.server.requests.PinJottingRequest;
import com.boruminc.borumjot.android.server.requests.UpdateTaskRequest;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubtaskActivity extends JottingActivity {
    /* Views */
    private MaterialToolbar appBar;
    private EditText taskDescriptionBox;
    private CheckBox taskCompletionBox;
    private TableLayout subtaskList;
    private EditTextV2 newSubtaskField;
    private TextView taskTitle;

    private Intent nextIntent;

    /* Overriding Callback Methods */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subtask_activity);

        taskDescriptionBox = findViewById(R.id.task_description_box);
        taskCompletionBox = findViewById(R.id.complete_task_btn);
        subtaskList = findViewById(R.id.task_subtasks_box);
        taskTitle = findViewById(R.id.task_title);

        // Set the userApiKey for class use
        if (getSharedPreferences("user identification", Context.MODE_PRIVATE) != null) {
            setUserApiKey(getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", ""));
        }

        appBar = findViewById(R.id.appbar);
        appBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.delete_btn:
                    onDeleteClick();
                    break;
                case R.id.pin_btn:
                    onPinClick(item);
                    break;
                case R.id.toggle_completed_tasks:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        onToggleCompletedTasks(item);
                    }
                default:
                    return false;
            }

            return true;
        });

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
                            Toast.makeText(SubtaskActivity.this, "The task could not be created at this time. ",
                                    Toast.LENGTH_SHORT).show();
                        } else if (data.optInt("statusCode") >= 200 && data.optInt("statusCode") < 300) {
                            getJottingData().setName(titleTextView.getText().toString());
                            getJottingData().setId(data.getJSONObject("data").getInt("id"));
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
        else if (getIntent().hasExtra("id")) {
            int subtaskId = getIntent().getIntExtra("id", 0);
            new SubtaskRetrieval(subtaskId).runAsync(getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", ""), new ApiResponseExecutor() {
                @Override
                public void onComplete(JSONObject result) {
                    super.onComplete(result);
                    try {
                        if (ranOk()) {
                            setJottingData(JSONToModel.convertJSONToTask(result.getJSONObject("data")));
                            setJottingName(getJottingData().getName());
                            setTaskDetails(getJottingData().getBody());
                            setTaskStatus(getTaskData().isCompleted());
                            setTaskPriority(getTaskData().getPriority());
                            setDueDate(getTaskData().getDueDate());
                            loadSubtasks(); // Set subtasks asynchronously
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        taskDescriptionBox.setOnFocusChangeListener(this::onDetailsBoxFocus);
        findViewById(R.id.appbar).setOnLongClickListener(this::onRenameJotting);

        if (getJottingData() == null) return;

        handleDueDates();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onToggleCompletedTasks(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.show_completed_tasks))) {
            setSubtasksTableContent(getTaskData().getSubtasks());
            item.setTitle(getString(R.string.hide_completed_tasks));
        } else {
            Stream<Task> incompleteTasks = getTaskData().getSubtasks().stream().filter(subtask -> !subtask.isCompleted());
            ArrayList<Task> incompleteTasksList = (ArrayList<Task>) incompleteTasks.collect(Collectors.toList());
            setSubtasksTableContent(incompleteTasksList);
            item.setTitle(getString(R.string.show_completed_tasks));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setTaskPriority(int priority) {
        if (priority == 1) {
            appBar.getMenu().findItem(R.id.pin_btn).setIcon(getResources().getDrawable(R.drawable.ic_filled_pin, getTheme()));
        }
    }

    private void onPinClick(MenuItem item) {
        new TaskRunner().executeAsync(new PinJottingRequest(getUserApiKey(), getJottingData()), new ApiResponseExecutor() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);
                if (ranOk()) {
                    if (getJottingData().getPriority() == 0) {
                        getJottingData().setPriority(1);
                        item.setIcon(
                                getResources().getDrawable(R.drawable.ic_filled_pin, getTheme())
                        );
                    } else {
                        getJottingData().setPriority(0);
                        item.setIcon(getResources().getDrawable(R.drawable.ic_outline_pin, getTheme()));
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    private void setSubtask(TextView view, boolean isCompleted) {
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
                            setSubtasksTableContent(subtaskData);
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
     *
     * @param name The new name of the jotting
     */
    protected void setJottingName(String name) {
        taskTitle.setText(name);
    }

    /**
     * Convenience method for getting the jotting data with the Task type
     *
     * @return The jotting data as a Task object
     */
    private Task getTaskData() {
        return (Task) getJottingData();
    }

    /**
     * Gets the UI's task details
     *
     * @return The currently displayed text of the description box
     */
    private String getTaskDetails() {
        return taskDescriptionBox.getText().toString();
    }

    /**
     * Sets the task details in the UI
     *
     * @param body The new body of the task
     */
    private void setTaskDetails(String body) {
        taskDescriptionBox.setText(SlashNormalizer.unescapeUserSlashes(body));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setTaskStatus(boolean on) {
        displayStrikethrough(on);
        taskCompletionBox.setChecked(on);
    }

    private void displayStrikethrough(boolean on) {
        if (on)
            taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            taskTitle.setPaintFlags(taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    private void setSubtasksTableContent(ArrayList<Task> subtasks) {
        subtaskList.removeAllViews();

        TableRow.LayoutParams subtaskTitleColumnLayoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtaskTitleColumnLayoutParams.setMargins(10, 0, 10, 0);

        subtaskList.setColumnShrinkable(2, true);

        for (Task subtask : subtasks) {
            subtaskList.addView(addSubtaskToTable(subtask));
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

    private LinearLayout addSubtaskToTable(Task subtask) {
        LinearLayout horizLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.subtask, null);

        TextView title = horizLayout.findViewById(R.id.subtask_title);
        title.setText(SlashNormalizer.unescapeUserSlashes(subtask.getName()));
        title.setTag(subtask.getId());
        title.setOnFocusChangeListener(this::onSubtaskBoxFocus);
        // Display strikethrough if the subtask is marked as complete
        if (subtask.isCompleted())
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        CheckBox checkBox = horizLayout.findViewById(R.id.completed_status);
        checkBox.setChecked(subtask.isCompleted());

        return horizLayout;
    }

    /* Event Handlers */

    public void onEnterSubtaskClick(View v) {
        Intent subtask = new Intent(getApplicationContext(), SubtaskActivity.class);
        subtask.putExtra("id", (int) v.getTag());
        startActivity(subtask);
    }

    public void onDeleteClick() {
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
                                            Toast.makeText(this, "The subtask could not be deleted due to a system error", Toast.LENGTH_SHORT).show();
                                        } else {
                                            finish();
                                            Toast.makeText(this, "Subtask deleted", Toast.LENGTH_SHORT).show();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onCompleteClick(View view) {
        boolean completed = ((CheckBox) view).isChecked();
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
                        return this.connectToApi(encodeQueryString("task", "completed=" + (completed ? 1 : 0), "id=" + getTaskData().getId()));
                    }
                },
                new ApiResponseExecutor() {
                    @Override
                    public void onComplete(JSONObject data) {
                        super.onComplete(data);
                        if (ranOk()) {
                            displayStrikethrough(completed);
                        } else {
                            Toast.makeText(getApplicationContext(), "An error occurred and the task could not be marked as "
                                    + (completed ? "completed" : "incomplete"), Toast.LENGTH_LONG).show();
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
                }, new ApiResponseExecutor() {
                    @Override
                    public void onComplete(JSONObject result) {
                        super.onComplete(result);
                        try {
                            if (ranOk()) {
                                Task subtask = new Task(newSubtaskField.getText().toString());
                                subtask.setId(result.getJSONObject("data").getInt("id"));

                                int indexAfterLastIncompleteTask = searchForLastIncompleteTask(subtask);
                                subtaskList.addView(addSubtaskToTable(subtask), indexAfterLastIncompleteTask);

                                ((EditText) subtaskList.findViewById(R.id.newSubtaskFieldId)).setText("");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
    /**
     * Add right after last incomplete task using binary search for the last incomplete subtask
     * @implNote The algorithm is as follows:
     * <ol>
     *     <li>Start at first element and check if complete</li>
     *     <li>If complete, then</li>
     *     <li>If the first element OR the previous element is incomplete, then</li>
     *          ADD SUBTASK; END
     *      <li>Else, (previous element is complete), then
     *          (the last incomplete subtask is BEFORE i) divide i by 2 and REPEAT
     *      <li>Else, (current element is incomplete), then</li>
     *          (the last incomplete subtask is AFTER i) set i to be between its current value
     *          and the index of the last element</li>
     * </ol>
     * @return one index after the last incomplete subtask, or 0 if there were no subtasks
     */
    private int searchForLastIncompleteTask(Task subtask) {
        ArrayList<Task> currSubtasks = getTaskData().getSubtasks();
        int subtaskSize = currSubtasks.size();
        int i = subtaskSize / 2;

        while (i < subtaskSize) {
            if (currSubtasks.get(i).isCompleted()) {
                if (i == 0 || !currSubtasks.get(i - 1).isCompleted()) {
                    getTaskData().addSubtask(i, subtask);

                    return i;
                } else {
                    i /= 2;
                }
            } else {
                if (i != currSubtasks.size() - 1 && currSubtasks.get(i + 1).isCompleted()) {
                    getTaskData().addSubtask(i + 1, subtask);
                    return i + 1;
                } else {
                    i = (int) Math.round((currSubtasks.size() + i) / 2.0);
                }
            }
        }

        // There are no (incomplete or complete) subtasks
        return currSubtasks.size();
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
                            TextView subtaskTxt = (TextView) subtaskRow.getChildAt(1);
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
                    new UpdateTaskRequest(getUserApiKey(), new String[]{"id=" + getJottingData().getId()}, new String[]{getTaskDetails()}), data -> {
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

        new TaskRunner().executeAsync(new UpdateTaskRequest(getUserApiKey(), new String[]{"id=" + id, "name=" + contents}, null), data -> {
            if (data == null || data.has("error")) {
                Toast.makeText(this, "A system error occurred and the subtask could not update", Toast.LENGTH_LONG).show();
            }
        });
    }
}

