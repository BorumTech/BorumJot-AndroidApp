package com.boruminc.borumjot.android;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONObject;

abstract class JottingActivity extends FragmentActivity {
    private String jottingType;
    private String userApiKey;
    private Jotting jottingData;

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

    /**
     * @param onPositiveButtonClick The event handler to call when the user presses the Save button
     * Displays the rename and name dialog in an <code>AlertDialog.Builder</code>
     */
    protected void displayRenameDialog(DialogInterface.OnClickListener onPositiveButtonClick) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            View renameJottingDialog = getLayoutInflater().inflate(R.layout.rename_jotting_dialog, null);

            if (getJottingData() != null) // If dialog if for renaming and not naming
                ((EditText) renameJottingDialog.findViewById(R.id.jot_name_edit)).setText(getJottingData().getName());

            AlertDialog.Builder renameBuilder = new AlertDialog.Builder(this);
            renameBuilder
                    .setView(renameJottingDialog)
                    .setTitle(String.format("%s Name", jottingType))

                    .setCancelable(true)
                    .setOnCancelListener(dialog -> finish())
                    .setPositiveButton("Save", onPositiveButtonClick);
            renameBuilder.create().show();
        } else {
            Toast.makeText(this, "Your phone is too old to name or rename the " + jottingType.toLowerCase() + ". You can only rename on the website", Toast.LENGTH_LONG).show();
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

    /**
     * Updates the UI of the appbar to display the passed in text
     * @param name The new name of the jotting
     */
    abstract void setJottingName(String name);
}
