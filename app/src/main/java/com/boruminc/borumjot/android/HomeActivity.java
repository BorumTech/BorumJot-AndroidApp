package com.boruminc.borumjot.android;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.labels.LabelActivity;
import com.boruminc.borumjot.android.labels.NewLabelActivity;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

@RequiresApi(api = Build.VERSION_CODES.N)
public class HomeActivity extends AppCompatActivity {
    private static final String JOTTINGS_ERROR = "The tasks and notes could not fetched at this time";

    private ExpandableJottingsListAdapter jottingsListAdapter;

    private JottingsListDataPump jotListData;
    private String userApiKey;

    /* Views */
    Button filterTasksBtn;
    Button filterNotesBtn;
    ProgressBar progressBar;
    SwipeRefreshLayout jottingsListRefresh;
    ExpandableListView expandableListView;
    View labelsList;
    ImageView addJotBtn;
    ExtendedFloatingActionButton addLabelBtn;
    private MenuItem labelMenuItem;

    /* Overriding Callback Methods */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar(findViewById(R.id.my_toolbar));
        findViewById(R.id.jotting_options_toolbar).setVisibility(View.INVISIBLE);

        // Set the views
        filterNotesBtn = findViewById(R.id.home_notes_toggle);
        filterTasksBtn = findViewById(R.id.home_tasks_toggle);
        progressBar = findViewById(R.id.progressPanel);
        expandableListView = findViewById(R.id.home_jottings_list);
        labelsList = findViewById(R.id.labels_list_frag);
        addJotBtn = findViewById(R.id.add_jotting_btn_home);
        addLabelBtn = findViewById(R.id.add_label_btn);

        labelsList.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        labelsList.setVisibility(View.GONE);

        // Set the user api key
        userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");

        // Set list adapter and related variables
        jottingsListAdapter = new ExpandableJottingsListAdapter(this);
        jotListData = new JottingsListDataPump();

        expandableListView.setAdapter(jottingsListAdapter);
        expandableListView.expandGroup(0);

        // Set the refresh listener
        jottingsListRefresh = findViewById(R.id.refreshable_jottings_list);
        jottingsListRefresh.setOnRefreshListener(this::onJottingsListRefresh);
    }

    @Override
    protected void onResume() {
        super.onResume();

        toggleFilter(filterTasksBtn, true);
        toggleFilter(filterNotesBtn, true);

        progressBar.setVisibility(View.VISIBLE);

        onJottingsListRefresh();
    }

    @Override
    public void onBackPressed() {
        if (labelsList.getVisibility() == View.VISIBLE) {
            hideLabelsList();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (findViewById(R.id.my_toolbar).getVisibility() == View.VISIBLE) { // If regular toolbar is active
            inflater.inflate(R.menu.options_menu, menu);

            if (menu != null) {
                labelMenuItem = menu.findItem(R.id.labels_btn);
            }
        }

        return true;
    }

    /**
     * Starts the corresponding activity based on which menu item was selected;
     * overrides pending transition
     * @param item The menu item object
     * @return true if a valid option was selected, false (the super method) otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_btn:
                startActivity(new Intent(this, OptionsActivity.class));
                return true;
            case R.id.help_btn:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            case R.id.labels_btn:
                if (labelsList.getVisibility() == View.VISIBLE) {
                    Log.d("labelMenuItem", String.valueOf(labelMenuItem));
                    hideLabelsList();
                } else {
                    labelsList.setVisibility(View.VISIBLE);
                    item.setIcon(getResources().getDrawable(R.drawable.label_white_fill));
                    addJotBtn.setVisibility(View.GONE);
                    addLabelBtn.setVisibility(View.VISIBLE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hideLabelsList() {
        labelsList.setVisibility(View.GONE);
        labelMenuItem.setIcon(getResources().getDrawable(R.drawable.label_white_outline));
        addJotBtn.setVisibility(View.VISIBLE);
        addLabelBtn.setVisibility(View.GONE);
    }

    /* Jottings */

    ApiRequestExecutor getJottingsRequest() {
        return new ApiRequestExecutor() {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("GET");
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString("jottings"));
            }
        };
    }

    ApiResponseExecutor handleJottingsResponse() {
        return new ApiResponseExecutor() {
            @Override
            public void onComplete(JSONObject data) {
                super.onComplete(data);
                progressBar.setVisibility(View.GONE); // Remove progress bar because the request is complete
                try {
                    if (ranOk()) { // If data was returned
                        jotListData.setOwnData(JSONToModel.convertJSONToJottings(data.getJSONArray("data")));
                        jottingsListAdapter.setOwnData(jotListData.getOwnData());
                        jottingsListAdapter.notifyDataSetChanged();

                        toggleFilter(filterTasksBtn, true);
                        toggleFilter(filterNotesBtn, true);
                    } else if (data != null && data.has("error") && data.getJSONObject("error").has("message")) {
                        Log.e("Fetch Error", data.getJSONObject("error").getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), JOTTINGS_ERROR, Toast.LENGTH_LONG).show();
                } finally {
                    jottingsListRefresh.setRefreshing(false);
                }
            }
        };
    }

    ApiRequestExecutor getSharedJottingsRequest() {
        return new ApiRequestExecutor() {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("GET");
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString("sharedjottings"));
            }
        };
    }

    ApiResponseExecutor handleSharedJottingsResponse() {
        return new ApiResponseExecutor() {
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);
                try {
                    if (ranOk()) {
                        JSONObject groupedData = result.getJSONObject("data");

                        ArrayList<Note> notes = JSONToModel.convertJSONToNotes(groupedData.getJSONArray("notes"));
                        ArrayList<Task> tasks = JSONToModel.convertJSONToTasks(groupedData.getJSONArray("tasks"));

                        ArrayList<Jotting> jottings = new ArrayList<>(notes);
                        jottings.addAll(tasks);

                        jotListData.setSharedData(jottings);
                        jottingsListAdapter.setSharedData(jotListData.getSharedData());
                        jottingsListAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "An error occurred and the shared jottings could not be fetched", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    /* Event Handlers */

    /**
     * Event handler for when the user swipe refreshes the jottings list adapter
     */
    public void onJottingsListRefresh() {
        new TaskRunner().executeAsync(getJottingsRequest(), handleJottingsResponse());
        new TaskRunner().executeAsync(getSharedJottingsRequest(), handleSharedJottingsResponse());
    }

    /**
     * Event handler for when the floating action button is clicked;
     * creates a micro-UI for adding a new jotting by prompting the user to select a type
     * @param view The view of the floating action plus button
     */
    public void onFloatingActionBtnClick(View view) {
        AtomicReference<Intent> activityToStart = new AtomicReference<Intent>();

        AlertDialog.Builder jotTypeDialogBuilder = new AlertDialog.Builder(this);
        jotTypeDialogBuilder
                .setTitle("New Jotting")
                .setIcon(R.drawable.icon)
                .setSingleChoiceItems(
                        getResources().getStringArray(R.array.jotting_types),
                        -1,
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    Intent activityIntent = new Intent(HomeActivity.this, NoteActivity.class);
                                    activityIntent.putExtra("Rename", true);
                                    activityToStart.set(activityIntent);
                                    break;
                                case 1:
                                    activityIntent = new Intent(this, TaskActivity.class);
                                    activityIntent.putExtra("Rename", true);
                                    activityToStart.set(activityIntent);
                                    break;
                            }
                        }
                )
                .setPositiveButton("OK", (dialog, whichButton) -> {
                    if (activityToStart.get() == null) {
                        Toast.makeText(this, "You must select a jotting type", Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(activityToStart.get());
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {});

        AlertDialog jotTypeDialog = jotTypeDialogBuilder.create();
        jotTypeDialog.show();

        // Initially disable the button
        jotTypeDialog.setOnShowListener((dialog) -> {
            jotTypeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        });

    }

    public void onAddLabelFABClick(View v) {
        startActivity(new Intent(this, NewLabelActivity.class));
    }

    /**
     * Toggles the display of notes.
     * Toggles the border,
     * toggles whether the user's notes are shown,
     * toggles the tag of the button (on or off)
     * @param view The button that is used to toggle the filtration of notes from the list
     */
    public void onToggleNotesFilter(View view) {
        final ArrayList<Jotting> dataset = jotListData.getOwnData();

        toggleFilter(view);
        if (dataset != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (view.getTag().equals("off")) { // Filter OUT
                ArrayList<Jotting> ownData = new ArrayList<>(jottingsListAdapter.getOwnData());
                ownData.removeIf(jotting -> jotting instanceof Note);
                jottingsListAdapter.setOwnData(ownData);
            } else if (view.getTag().equals("on")) { // Add BACK
                for (Jotting jotting : jotListData.getOwnData()) {
                    if (jotting instanceof Note) {
                        jottingsListAdapter.getOwnData().add(jotting);
                    }
                }
            }
        } else {
            Toast.makeText(this, "Your device is too old to support jotting filtering", Toast.LENGTH_LONG).show();
        }

        jottingsListAdapter.notifyDataSetChanged();
    }

    /**
     * Displays or stops displaying the current tasks in the list.
     * @param view The button that is used to toggle the filtration of tasks from the list
     */
    public void onToggleTasksFilter(View view) {
        toggleFilter(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (view.getTag().equals("off")) { // Filter OUT
                ArrayList<Jotting> ownData = new ArrayList<>(jottingsListAdapter.getOwnData());
                ownData.removeIf(jotting -> jotting instanceof Task);
                jottingsListAdapter.setOwnData(ownData);
            } else if (view.getTag().equals("on")) { // Add BACK
                ArrayList<Jotting> originalDataset = jotListData.getOwnData();
                for (Jotting jotting : originalDataset) {
                    if (jotting instanceof Task) {
                        jottingsListAdapter.getOwnData().add(jotting);
                    }
                }
            }

            jottingsListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Your device is too old to support jotting filtering", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Toggles the border and resets the tag of the button
     * @param view The view that is filtering the jotting type
     */
    public void toggleFilter(View view) {
        if (view.getTag().equals("on")) {
            toggleFilter(view, false);
        } else {
            toggleFilter(view, true);
        }
    }

    /**
     * Sets or removes the border based on passed in flag
     * @param view The view that is filtering the jotting type
     * @param flag True to add the border; false to remove border
     */
    public void toggleFilter(View view, boolean flag) {
        view.setTag(flag ? "on" : "off");
        ((MaterialButton) view).setStrokeWidth(flag ? (int) getResources().getDimension(R.dimen.filter_btn_activated) : 0);
    }
}
