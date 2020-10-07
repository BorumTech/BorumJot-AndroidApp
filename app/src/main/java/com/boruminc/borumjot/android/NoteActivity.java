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
import android.widget.RelativeLayout;
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

    enum Mode {
        RENAME,
        VIEW
    }

    private Mode currentMode;

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

        if (getIntent().getBooleanExtra("Rename", false)) {
            displayRenameDialog();
            currentMode = Mode.RENAME;
        }
        else if (getIntent().hasExtra("data")) {
            noteData = (Note) getIntent().getSerializableExtra("data");
            assert noteData != null;
            setNoteName(noteData.getName());
            setNoteBody(noteData.getBody());

            currentMode = Mode.VIEW;
        }

        noteDescriptionBox.setOnFocusChangeListener(this::onDetailsBoxFocus);
    }

    /**
     * Updates the UI of the appbar to display the passed in text
     * @param name The new name of the task
     */
    private void setNoteName(String name) {
        if (appBarFrag != null) appBarFrag.passTitle(name);
    }

    private void setNoteBody(String body) {
        noteDescriptionBox.setText(body == null || body.equals("null") ? "" : body);
    }

    private String getNoteBody() {
        return noteDescriptionBox.getText().toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentMode == null || currentMode.equals(Mode.RENAME)) return;

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
                        if (data != null && data.has("data") && data.getInt("statusCode") >= 200 && data.getInt("statusCode") < 300) {
                            // If response ran okay
                            setNoteBody(data.getJSONObject("data").getString("body"));
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

    /**
     * Displays the rename and name dialog in an <code>AlertDialog.Builder</code>
     */
    private void displayRenameDialog() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            androidx.appcompat.app.AlertDialog.Builder renameBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
            renameBuilder
                    .setView(getLayoutInflater().inflate(R.layout.rename_jotting_dialog, null))
                    .setTitle("Note Name")
                    .setCancelable(true)
                    .setOnCancelListener(dialog -> finish())
                    .setPositiveButton("Save", (dialog, which) -> {
                        TextView titleTextView = ((Dialog) dialog).findViewById(R.id.jot_name_edit);

                        if (titleTextView == null) { // Display error and exit if appbar title could not be found
                            Toast.makeText(this, "An error occurred and you cannot name the note at this time. ", Toast.LENGTH_LONG).show();
                            return;
                        }

                        setNoteName(titleTextView.getText().toString());
                        new TaskRunner().executeAsync(new ApiRequestExecutor(titleTextView.getText().toString()) {
                            @Override
                            protected void initialize() {
                                super.initialize();
                                setQuery(this.encodePostQuery("name=%s"));
                                setRequestMethod("POST");

                                // Set the user's api key or an empty string in the Authentication request header
                                addRequestHeader("Authorization", "Basic " + userApiKey);
                            }

                            @Override
                            public JSONObject call() {
                                super.call();
                                return this.connectToApi(encodeUrl("note"));
                            }
                        }, data -> {
                            try {
                                if (data != null) {
                                    if (data.has("error") && data.getJSONObject("error").has("message")) {
                                        Toast.makeText(NoteActivity.this, "The note could not be created at this time. ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                    });
            renameBuilder.create().show();
        } else {
            Toast.makeText(this, "Your phone is too old to name or rename the note. You can only rename on the website", Toast.LENGTH_LONG).show();
        }
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

    public void onDetailsBoxFocus(View view, boolean isFocused) {
        Log.d("Focused", String.valueOf(isFocused));
        if (!isFocused) {

            if (!noteData.getBody().equals(getNoteBody())) {
                new TaskRunner().executeAsync(
                        new ApiRequestExecutor(getNoteBody()) {
                            @Override
                            protected void initialize() {
                                super.initialize();
                                setRequestMethod("PUT");
                                addRequestHeader("Authorization", "Basic " + userApiKey);
                                setQuery(encodePostQuery("body=%s"));
                            }

                            @Override
                            public JSONObject call() {
                                super.call();
                                return this.connectToApi(encodeUrl("note", "id=" + noteData.getId()));
                            }
                        }, data -> {
                            try {
                                if (data != null) {
                                    if (data.has("error"))
                                        Toast.makeText(this, data.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "The note was not updated due to a system error", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "An unknown error occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        }
    }
}
