package com.boruminc.borumjot.android;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.android.labels.JotLabelsList;
import com.boruminc.borumjot.android.labels.UpdateJottingLabels;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class JottingActivity extends AppCompatActivity {
    private String jottingType;
    private String userApiKey;
    private Jotting jottingData;
    private ArrayList<Label> allUserLabels;
    private AlertDialog.Builder alertDialog;
    private JotLabelsList labelsList;

    /* Getters and Setters */

    public void setJottingType(String jottingType) {
        this.jottingType = jottingType;
    }

    public String getUserApiKey() {
        return userApiKey;
    }

    protected void setLabelsList(JotLabelsList labelsList) { this.labelsList = labelsList; }

    protected JotLabelsList getLabelsList() { return labelsList; }

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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            View renameJottingDialog = getLayoutInflater().inflate(R.layout.rename_jotting_dialog, null);

            if (getJottingData() != null) // If dialog if for renaming and not naming
                ((EditText) renameJottingDialog.findViewById(R.id.jot_name_edit)).setText(getJottingData() == null ? "" : getJottingData().getName());

            AlertDialog.Builder renameBuilder = new AlertDialog.Builder(this);
            renameBuilder
                    .setView(renameJottingDialog)
                    .setTitle(String.format("%s Name", jottingType))

                    .setCancelable(true)
                    .setOnCancelListener(dialog -> finish())
                    .setPositiveButton("Save", onPositiveButtonClick)
                    .setOnDismissListener(DialogInterface::dismiss);
            renameBuilder.create().show();
        } else {
            Toast.makeText(this, "Your phone is too old to name or rename the " + jottingType + ". Try on the web app", Toast.LENGTH_LONG).show();
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
                return this.connectToApi(encodeQueryString("labels", "jot_type=" + jottingType, "id=" + getJottingData().getId()));
            }
        };
    }

    /* API Response Handlers */

    protected void loadNewLabels(JSONObject data, String newLabelName) {
        try {
            if (data != null) {
                if (data.optInt("statusCode") >= 200 && data.optInt("statusCode") < 300) {
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
                            return this.connectToApi(encodeQueryString(
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
                new UpdateJottingLabels(getJottingData(), jottingType).runAsync(
                        getSharedPreferences("user identification", Context.MODE_PRIVATE)
                                .getString("apiKey", ""),
                        new ApiResponseExecutor() {
                            @Override
                            public void onComplete(JSONObject result) {
                                super.onComplete(result);
                                if (!ranOk())
                                    Toast.makeText(getApplicationContext(), "A system error occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            })
            .setNegativeButton("Cancel", (dialog, which) -> {});

        alertDialog.create().show();
    }

    protected void onLabelListClick(MenuItem item) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_down, R.anim.slide_up);

        // Hide and show to constrain label note request to one and show animation
        if (getLabelsList().isHidden()) {
            item.setIcon(getResources().getDrawable(R.drawable.label_white_fill));
            ft.show(getLabelsList()).commit();
        } else {
            item.setIcon(getResources().getDrawable(R.drawable.label_white_outline));
            slideLabelsListUp(ft);
        }
    }

    private void onLabelsListChoiceClick(DialogInterface dialog, int which, boolean checked) {
        if (checked) {
            getJottingData().addLabel(allUserLabels.get(which));
        } else {
            getJottingData().removeLabel(allUserLabels.get(which));
        }
        Log.d("Jotting labels", getJottingData().getLabels().toString());
    }

    protected void slideLabelsListDown() {
        labelsList.requireView()
                .animate()
                .translationY(0);
    }

    protected void slideLabelsListUp(FragmentTransaction ft) {
        labelsList.requireView()
                .animate()
                .translationY(-labelsList.requireView().getHeight())
                .withEndAction(() -> {
                    ft.hide(labelsList).commit();
                    slideLabelsListDown();
                    Log.d("Visibility", String.valueOf(labelsList.isVisible()));
                });
    }

    /* Abstract Methods */

    /**
     * Updates the UI of the appbar to display the passed in text
     * @param name The new name of the jotting
     */
    protected abstract void setJottingName(String name);
}
