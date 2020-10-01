package com.boruminc.borumjot.android;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class HomeActivity extends AppCompatActivity {
    private static final String JOTTINGS_ERROR = "The jottings could not be fetched at this time.";

    RecyclerView recyclerView;
    JottingsListAdapter jottingsListAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar(findViewById(R.id.my_toolbar));

        recyclerView = findViewById(R.id.home_jottings_list);

        // Improve performance because changes in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Specify an adapter (see also next example)
        jottingsListAdapter = new JottingsListAdapter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ProgressBar progressBar = findViewById(R.id.progressPanel);
        progressBar.setVisibility(View.VISIBLE);
        new TaskRunner().executeAsync(new ApiRequestExecutor() {
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
                return this.connectToApi(encodeUrl("tasks", "app_api_key=a9sd8a9d8as09da8s9d", "type=tasks"));
            }
        }, data -> {
            progressBar.setVisibility(View.GONE); // Remove progress bar because the request is complete
            try {
                if (data != null) {
                    if (data.has("data") && data.getInt("statusCode") == 200) { // If data was returned
                        ArrayList<Jotting> userJottings = new ArrayList<Jotting>(data.getInt("rowCount"));
                        JSONArray jottingsData = data.getJSONArray("data");
                        for (int i = 0; i < jottingsData.length(); i++) {
                            JSONObject row = jottingsData.getJSONObject(i);
                            Task task = new Task(
                                    row.getString("task_name"),
                                    row.getString("task_details"),
                                    new ArrayList<Label>()
                            );
                            task.setId(row.getInt("task_id"));
                            task.setCompleted(row.getString("task_completed").equals("1"));
                            userJottings.add(task);
                        }

                        jottingsListAdapter.setDataset(userJottings);
                        recyclerView.setAdapter(jottingsListAdapter);
                        return;
                    } else if (data.has("error") && data.getJSONObject("error").has("message")) {
                        Log.e("Fetch Error", data.getJSONObject("error").getString("message"));
                    }
                }
                Toast.makeText(this, JOTTINGS_ERROR, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, JOTTINGS_ERROR, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // Starts SearchResultsActivity when
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
            case R.id.helpandfdbck_btn:
                startActivity(new Intent(this, HelpAndFeedbackActivity.class));
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


}
