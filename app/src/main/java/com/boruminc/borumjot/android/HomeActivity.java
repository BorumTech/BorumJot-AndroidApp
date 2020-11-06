package com.boruminc.borumjot.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class HomeActivity extends AppCompatActivity {
    private static final String JOTTINGS_ERROR = "The tasks and notes could not fetched at this time";

    ExpandableJottingsListAdapter jottingsListAdapter;
    ArrayList<Jotting> originalDataset;
    HashMap<String, ArrayList<Jotting>> fullExpandableJottingMap;

    /* Views */
    Button filterTasksBtn;
    Button filterNotesBtn;
    ProgressBar progressBar;
    SwipeRefreshLayout jottingsListRefresh;
    ExpandableListView expandableListView;

    /* Overriding Callback Methods */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar(findViewById(R.id.my_toolbar));
        findViewById(R.id.jotting_options_toolbar).setVisibility(View.INVISIBLE);

        filterNotesBtn = findViewById(R.id.home_notes_toggle);
        filterTasksBtn = findViewById(R.id.home_tasks_toggle);
        progressBar = findViewById(R.id.progressPanel);
        expandableListView = findViewById(R.id.home_jottings_list);

        AppBarFragment appBarFragment = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.my_toolbar);
        if (appBarFragment != null) appBarFragment.passTitle("My Jottings");

        // Specify an adapter (see also next example)
        jottingsListAdapter = new ExpandableJottingsListAdapter(this);
        fullExpandableJottingMap = JottingsListDataPump.getData();
        expandableListView.setAdapter(jottingsListAdapter);
        expandableListView.setOnGroupExpandListener(groupPosition -> {

        });

        expandableListView.setOnGroupCollapseListener(groupPosition -> {

        });

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            //
            return true;
        });

        // Set the refresh listener
        jottingsListRefresh = findViewById(R.id.refreshable_jottings_list);
        jottingsListRefresh.setOnRefreshListener(this::onJottingsListRefresh);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new TaskRunner().executeAsync(getJottingsRequest(), this::handleJottingsResponse);
    }

    @Override
    protected void onResume() {
        super.onResume();

        toggleFilter(filterTasksBtn, true);
        toggleFilter(filterNotesBtn, true);

        progressBar.setVisibility(View.VISIBLE);

        originalDataset = new ArrayList<Jotting>();
        onJottingsListRefresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (findViewById(R.id.my_toolbar).getVisibility() == View.VISIBLE) { // If regular toolbar is active
            inflater.inflate(R.menu.options_menu, menu);
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
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.options_btn:
                startActivity(new Intent(this, OptionsActivity.class));
                break;
            case R.id.privacypolicy_btn:
                startActivity(new Intent(this, PrivacyPolicyActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        // Animate the activity switch so that it slides the home activity out to the left and slides in the new OptionsItemActivity
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        return true;
    }

    /* Helper Methods */

    private ApiRequestExecutor getJottingsRequest() {
        return new ApiRequestExecutor() {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("GET");
                addRequestHeader("Authorization", "Basic " +
                        getSharedPreferences("user identification", Context.MODE_PRIVATE)
                                .getString("apiKey", "")
                );
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString("jottings"));
            }
        };
    }

    private void handleJottingsResponse(JSONObject data) {
        progressBar.setVisibility(View.GONE); // Remove progress bar because the request is complete
        try {
            if (data != null) {
                if (data.has("data") && data.getInt("statusCode") == 200) { // If data was returned
                    JSONArray jottingsData = data.getJSONArray("data");
                    originalDataset.clear();

                    for (int i = 0; i < jottingsData.length(); i++) {
                        JSONObject row = jottingsData.getJSONObject(i);

                        if (row.getString("source").equals("note"))
                            originalDataset.add(JSONToModel.convertJSONToNote(row));
                        else if (row.getString("source").equals("task"))
                            originalDataset.add(JSONToModel.convertJSONToTask(row));
                    }

                    ArrayList<Jotting> ownList = fullExpandableJottingMap.get("own");
                    assert ownList != null;
                    ownList.clear();
                    ownList.addAll(originalDataset);
                    jottingsListAdapter.notifyDataSetChanged();

                    toggleFilter(filterTasksBtn, true);
                    toggleFilter(filterNotesBtn, true);

                    return;
                } else if (data.has("error") && data.getJSONObject("error").has("message")) {
                    Log.e("Fetch Error", data.getJSONObject("error").getString("message"));
                }
            }
            Toast.makeText(this, JOTTINGS_ERROR, Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, JOTTINGS_ERROR, Toast.LENGTH_LONG).show();
        } finally {
            jottingsListRefresh.setRefreshing(false);
        }
    }

    /* Event Handlers */

    public void onJottingsListRefresh() {
        new TaskRunner().executeAsync(getJottingsRequest(), this::handleJottingsResponse);
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
                                    Intent activityIntent = new Intent(this, NoteActivity.class);
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

    /**
     * Toggles the display of notes.
     * Toggles the border,
     * toggles whether the user's notes are shown,
     * toggles the tag of the button (on or off)
     * @param view The button that is used to toggle the filtration of notes from the list
     */
    public void onToggleNotesFilter(View view) {
        final ArrayList<Jotting> dataset = jottingsListAdapter.getDataset();

        toggleFilter(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (view.getTag().equals("off")) // Filter OUT
                jottingsListAdapter.getDataset().removeIf(jotting -> jotting instanceof Note);
            else if (view.getTag().equals("on")) { // Add BACK
                for (Jotting jotting : originalDataset) {
                    if (jotting instanceof Note) {
                        dataset.add(jotting);
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
        final ArrayList<Jotting> dataset = jottingsListAdapter.getDataset();

        toggleFilter(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (view.getTag().equals("off")) // Filter OUT
                dataset.removeIf(jotting -> jotting instanceof Task);
            else if (view.getTag().equals("on")) { // Add BACK
                for (Jotting jotting : originalDataset) {
                    if (jotting instanceof Task) {
                        dataset.add(jotting);
                    }
                }
            }
        } else {
            Toast.makeText(this, "Your device is too old to support jotting filtering", Toast.LENGTH_LONG).show();
        }

        jottingsListAdapter.notifyDataSetChanged();
    }

    /**
     * Sets or removes the border and resets the tag of the button
     * @param view The view that is filtering the jotting type
     */
    public void toggleFilter(View view) {
        GradientDrawable filterBtn = (GradientDrawable) view.getBackground();

        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorSecondary, a, true);

        if (view.getTag().equals("on")) {
            view.setTag("off");
            filterBtn.setStroke(0, a.data);
        } else {
            view.setTag("on");
            filterBtn.setStroke(10, a.data);
        }
    }

    public void toggleFilter(View view, boolean flag) {
        GradientDrawable filterBtn = (GradientDrawable) view.getBackground();

        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorSecondary, a, true);

        view.setTag(flag ? "on" : "off");
        filterBtn.setStroke(flag ? 10 : 0, a.data);
    }
}
