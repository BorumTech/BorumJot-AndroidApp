package com.boruminc.borumjot.android.labels;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.android.AppBarFragment;
import com.boruminc.borumjot.android.R;
import com.boruminc.borumjot.android.RecyclableJottingsListAdapter;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LabelActivity extends AppCompatActivity {
    /* Views */
    RecyclerView jottingListView;

    /* Custom Objects */
    AppBarFragment appBarFrag;
    Label currentLabel;
    ArrayList<Jotting> jottingListData;

    /* Primitives and Strings */
    String userApiKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);
        setSupportActionBar(findViewById(R.id.appbar));

        appBarFrag = ((AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar));

        View appBar = findViewById(R.id.appbar);

        if (appBarFrag == null || !getIntent().hasExtra("label")) {
            Toast.makeText(this, "The label screen is malfunctioning", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentLabel = (Label) getIntent().getSerializableExtra("label");
        assert currentLabel != null;
        appBarFrag.passTitle(currentLabel.getName());
        appBar.setOnLongClickListener(this::onRenameLabel);
        userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");

        displayJottingsWithCurrentLabel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.label_menu, menu);
        Log.d("Menu", String.valueOf(menu.getItem(0)));
        return true;
    }

    /**
     * Starts the corresponding activity based on which menu item was selected
     * @param item The menu item object
     * @return true if a valid option was selected, false (the super method) otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.delete_btn:
                new TaskRunner().executeAsync(deleteLabelRequest(), new ApiResponseExecutor() {
                    @Override
                    public void onComplete(JSONObject result) {
                        super.onComplete(result);
                        if (result != null && ranOk()) {
                            finish();
                            Toast.makeText(getApplicationContext(), "Label successfully deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "The label could not be deleted due to a system error", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void displayJottingsWithCurrentLabel() {
        jottingListView = findViewById(R.id.label_filtered_jotting_list);
        new TaskRunner().executeAsync(makeLabelJottingsRequest(), handleLabelJottingsResponse());
    }

    private ApiRequestExecutor makeLabelJottingsRequest() {
        return new ApiRequestExecutor(String.valueOf(currentLabel.getId())) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("GET");
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(
                        encodeQueryString("jottings", String.format("label=%s", currentLabel.getId()))
                );
            }
        };
    }

    private ApiResponseExecutor handleLabelJottingsResponse() {
        return new ApiResponseExecutor() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);
                try {
                    if (ranOk()) {
                        jottingListData = JSONToModel.convertJSONToJottings(result.getJSONArray("data"));

                        if (jottingListData.isEmpty()) {
                            findViewById(R.id.label_filtered_jotting_list).setVisibility(View.GONE);
                            findViewById(R.id.empty_label_empty_state_container).setVisibility(View.VISIBLE);
                            return;
                        }
                        RecyclableJottingsListAdapter adapter = new RecyclableJottingsListAdapter(
                                LabelActivity.this,
                                jottingListData
                        );
                        jottingListView.setAdapter(adapter);
                        jottingListView.setLayoutManager(new LinearLayoutManager(LabelActivity.this));
                    } else {
                        Toast.makeText(getApplicationContext(), "A server error occurred", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(LabelActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        };
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
                return this.connectToApi(encodeQueryString("label"));
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

    private ApiRequestExecutor deleteLabelRequest() {
        return new ApiRequestExecutor() {
            @Override
            protected void initialize() {
                super.initialize();
                addAuthorizationHeader(userApiKey);
                setRequestMethod("DELETE");
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString("label", "id=" + currentLabel.getId()));
            }
        };
    }
}
