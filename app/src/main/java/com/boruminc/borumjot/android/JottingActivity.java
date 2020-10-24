package com.boruminc.borumjot.android;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

abstract class JottingActivity extends FragmentActivity {
    private String jottingType;
    private String userApiKey;
    private Jotting jottingData;
    private ArrayList<Label> allUserLabels;
    private AlertDialog.Builder alertDialog;

    /* Getters and Setters */

    public void setJottingType(String jottingType) {
        this.jottingType = jottingType;
    }

    public String getUserApiKey() {
        return userApiKey;
    }

    public void setUserApiKey(String userApiKey) {
        this.userApiKey = userApiKey;
    }

    public Jotting getJottingData() {
        return jottingData;
    }

    public void setJottingData(Jotting jottingData) {
        this.jottingData = jottingData;
    }

    /* Helper Methods */

    /**
     * @param onPositiveButtonClick The event handler to call when the user presses the Save button
     * Displays the rename and name dialog in an <code>AlertDialog.Builder</code>
     */
    protected void displayRenameDialog(DialogInterface.OnClickListener onPositiveButtonClick) {
        displayEditTextDialog(
                onPositiveButtonClick,
                getJottingData() == null ? "" : getJottingData().getName(),
                "Your phone is too old to name or rename the " + jottingType,
                String.format("%s Name", jottingType)
        );
    }

    protected void displayEditTextDialog(DialogInterface.OnClickListener onPositiveButtonClick, String initialText, String errorMessage, String title) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            View renameJottingDialog = getLayoutInflater().inflate(R.layout.rename_jotting_dialog, null);

            if (getJottingData() != null) // If dialog if for renaming and not naming
                ((EditText) renameJottingDialog.findViewById(R.id.jot_name_edit)).setText(initialText);

            AlertDialog.Builder renameBuilder = new AlertDialog.Builder(this);
            renameBuilder
                    .setView(renameJottingDialog)
                    .setTitle(title)

                    .setCancelable(true)
                    .setOnCancelListener(dialog -> finish())
                    .setPositiveButton("Save", onPositiveButtonClick);
            renameBuilder.create().show();
        } else {
            Toast.makeText(this, errorMessage + ". Try on the web app", Toast.LENGTH_LONG).show();
        }
    }

    /* API Requests */

    /**
     * Returns the request to get the current jotting's labels
     * @return The ApiRequestExecutor object
     */
    protected ApiRequestExecutor getJottingLabels() {
        return new ApiRequestExecutor() {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("GET");
                addAuthorizationHeader(getUserApiKey());
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeUrl(jottingType.toLowerCase() + "/labels", "id=" + getJottingData().getId()));
            }
        };
    }

    /**
     * Returns the requests that creates a new label to be added to any jotting
     * @param newLabelName The name of the new label
     * @return The ApiRequestExecutor object
     */
    protected ApiRequestExecutor createNewLabel(String newLabelName) {
        return new ApiRequestExecutor(newLabelName) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("POST");
                addAuthorizationHeader(getUserApiKey());
                setQuery(encodePostQuery("name=%s"));
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeUrl("label"));
            }
        };
    }

    /**
     * Returns the request that adds or removes labels from the current jotting
     * @return The ApiRequestExecutor object
     */
    protected ApiRequestExecutor updateJottingLabels() {
        StringBuilder labelIds = new StringBuilder();
        for (int i = 0; i < getJottingData().getLabels().size(); i++)
            labelIds.append(getJottingData().getLabels().get(i).getId()).append(",");

        return new ApiRequestExecutor(String.valueOf(getJottingData().getId()), labelIds.toString()) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("PUT");
                addAuthorizationHeader(getUserApiKey());
                setQuery(this.formatPostQuery(jottingType.toLowerCase() + "_id=%s&label_ids=%s"));
                Log.d("Query", formatPostQuery(jottingType.toLowerCase() + "_id=%s&label_ids=%s"));
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeUrl(jottingType.toLowerCase() + "/labels"));
            }
        };
    }

    /* API Response Handlers */

    /**
     * Loads the labels
     * @param data The label data as a JSONObject
     */
    protected void loadLabels(JSONObject data) {
        if (data != null) {
            try {
                if (data.optInt("statusCode") == 200 && data.has("data")) {
                    allUserLabels = JSONToModel.convertJSONToLabels(data.getJSONArray("data"), true);
                    ArrayList<Label> taskLabels = JSONToModel.convertJSONToLabels(data.getJSONArray("data"), false);
                    setLabels(taskLabels);
                    getJottingData().setLabels(taskLabels);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected void loadNewLabels(JSONObject data, String newLabelName) {
        try {
            if (data != null) {
                if (data.optInt("statusCode") == 204) {
                    allUserLabels.add(new Label(data.getJSONObject("data").getInt("id"), newLabelName));

                    CharSequence[] labelNames = new CharSequence[allUserLabels.size()];
                    boolean[] labelStatuses = new boolean[allUserLabels.size()];
                    for (int i = 0; i < labelNames.length; i++) {
                        labelNames[i] = allUserLabels.get(i).getName();
                        // Set to if the task has the current label
                        labelStatuses[i] = getJottingData().getLabels().contains(allUserLabels.get(i));
                    }
                    alertDialog.setMultiChoiceItems(labelNames, labelStatuses, (dialogInner, whichInner, isChecked) -> {
                        getJottingData().addLabel(allUserLabels.get(whichInner));
                    });
                    alertDialog.create().show();
                } else {
                    Toast.makeText(this, "An error occurred and the label could not be created", Toast.LENGTH_LONG).show();
                    Log.e("API Request", data.getJSONObject("error").getString("message"));
                    Log.e("API Request", data.getJSONObject("error").getString("query"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* Event Handlers */

    public void onNewLabelClick(View view) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            View renameJottingDialog = getLayoutInflater().inflate(R.layout.rename_jotting_dialog, null);

            ((EditText) renameJottingDialog.findViewById(R.id.jot_name_edit)).setText("");

            AlertDialog.Builder renameBuilder = new AlertDialog.Builder(this);
            renameBuilder
                    .setView(renameJottingDialog)
                    .setTitle("Labels")
                    .setCancelable(true)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newLabelName =
                                ((EditText) ((Dialog) dialog).findViewById(R.id.jot_name_edit))
                                        .getText()
                                        .toString();

                        new TaskRunner().executeAsync(createNewLabel(newLabelName), data -> {
                            loadNewLabels(data, newLabelName);
                        });
                    });
            renameBuilder.create().show();
        } else {
            Toast.makeText(this, "Your phone is too old to create a new label. Try on the web app", Toast.LENGTH_LONG).show();
        }
    }

    protected boolean onRenameJotting(View view) {
        displayRenameDialog(((dialog, which) -> {
            TextView titleTextView = ((Dialog) dialog).findViewById(R.id.jot_name_edit);

            if (titleTextView == null) { // Display error and exit if appbar title could not be found
                Toast.makeText(this, "An error occurred and you cannot rename the " + jottingType + " at this time. ", Toast.LENGTH_LONG).show();
                return;
            }

            new TaskRunner().executeAsync(
                    new ApiRequestExecutor() {
                        @Override
                        protected void initialize() {
                            super.initialize();
                            setRequestMethod("PUT");
                            this.addRequestHeader("Authorization", "Basic " + userApiKey);
                        }

                        @Override
                        public JSONObject call() {
                            super.call();
                            return this.connectToApi(encodeUrl(
                                    jottingType.toLowerCase(),
                                    "id=" + jottingData.getId(),
                                    "name=" + titleTextView.getText().toString()
                            ));
                        }
                    }, data -> {
                        if (data != null) {
                            if (data.optInt("statusCode") == 200)
                                setJottingName(titleTextView.getText().toString());
                            else
                                Toast.makeText(this, "The " + jottingType.toLowerCase() + " could not be renamed due to a system error", Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }));

        return true;
    }

    protected void onLabelControlsBtnClick(View view) {
        CharSequence[] labelNames = new CharSequence[allUserLabels.size()];
        boolean[] labelStatuses = new boolean[allUserLabels.size()];
        for (int i = 0; i < labelNames.length; i++) {
            labelNames[i] = allUserLabels.get(i).getName();
            // Set to if the task has the current label
            labelStatuses[i] = getJottingData().getLabels().contains(allUserLabels.get(i));
        }

        alertDialog = new AlertDialog.Builder(this);

        alertDialog
                .setTitle("Labels")
                .setMultiChoiceItems(labelNames, labelStatuses, this::onLabelsListChoiceClick)
                .setView(R.layout.label_controls_dialog)
                .setCancelable(true)
                .setPositiveButton("Save", (dialog, which) -> {
                    new TaskRunner().executeAsync(updateJottingLabels(), data -> {
                        if (data == null || data.optInt("statusCode") != 200) {
                            Toast.makeText(this, "A system error occurred", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {});
        alertDialog.create().show();
    }

    private void onLabelsListChoiceClick(DialogInterface dialog, int which, boolean checked) {
        if (checked) {
            getJottingData().addLabel(allUserLabels.get(which));
        } else {
            getJottingData().removeLabel(allUserLabels.get(which));
        }
        Log.d("Jotting labels", getJottingData().getLabels().toString());
    }

    /* Abstract Methods */

    /**
     * Updates the UI of the appbar to display the passed in text
     * @param name The new name of the jotting
     */
    abstract void setJottingName(String name);

    abstract void setLabels(ArrayList<Label> labels);
}
