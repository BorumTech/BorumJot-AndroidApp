package com.boruminc.borumjot.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.boruminc.borumjot.android.server.requests.DeleteJottingRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class NoteActivity extends FragmentActivity {
    private Note noteData;
    private String userApiKey;

    /* Views */
    private AppNameAppBarFragment appBarFrag;
    private EditText noteDescriptionBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);

        findViewById(R.id.nav_to_share_btn).setOnClickListener(this::navigateToShare);
        findViewById(R.id.delete_note_btn).setOnClickListener(this::showDeleteDialog);
        noteDescriptionBox = findViewById(R.id.note_content);
        appBarFrag = (AppNameAppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);

        // Set the userApiKey for use throughout the class
        if (getSharedPreferences("user identification", Context.MODE_PRIVATE) != null) {
            userApiKey = getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", "");
        }

//        if (getIntent().getBooleanExtra("Rename", false)) displayRenameDialog();
        if (getIntent().hasExtra("data")) {
            noteData = (Note) getIntent().getSerializableExtra("data");
            assert noteData != null;
            setNoteName(noteData.getName());
            setNoteBody(noteData.getBody());
        }
    }

    /**
     * Updates the UI of the appbar to display the passed in text
     * @param name The new name of the task
     */
    private void setNoteName(String name) {
        if (appBarFrag != null) appBarFrag.passTitle(name);
    }

    private void setNoteBody(String body) {
        noteDescriptionBox.setText(body);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new TaskRunner().executeAsync(
                new ApiRequestExecutor() {
                    @Override
                    protected void initialize() {
                        super.initialize();
                        setRequestMethod("GET");
                        addRequestHeader("Authorization", "Basic " + userApiKey);
                    }

                    @Override
                    public JSONObject call() {
                        super.call();
                        return this.connectToApi(encodeUrl("note", "id=" + noteData.getId()));
                    }
                }, data -> {
                    try {
                        if (data != null && data.has("data") && data.getInt("statusCode") == 200) {
                            // If response ran okay
                            ((EditText) findViewById(R.id.note_content)).setText(data.getJSONObject("data").getString("body"));
                        } else {
                            Toast.makeText(this, "An error occurred and this note could not be fetched", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "An error occurred and this note could not be fetched", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public void navigateToShare(View view) {
        startActivity(new Intent(this, ShareActivity.class));
    }

    public void showDeleteDialog(View view) {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog
                .setTitle("Delete Note")
                .setMessage("Are you sure you would like to delete this note? All sharees will lose access as well")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new TaskRunner().executeAsync(
                        new DeleteJottingRequest(noteData.getId(), userApiKey, "note"),
                        data -> {
                            if (data != null) {
                                if (data.isNull("error") && data.optInt("statusCode") == 200) {
                                    startActivity(new Intent(this, HomeActivity.class));
                                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "An error occurred and the note could not be deleted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    );

                })
                .setNegativeButton("Cancel", (dialog, which) -> {});
        deleteDialog.create().show();
    }
}
