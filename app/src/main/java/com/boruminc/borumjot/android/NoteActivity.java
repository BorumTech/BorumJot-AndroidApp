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

import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.boruminc.borumjot.android.server.requests.DeleteJottingRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NoteActivity extends JottingActivity {
    /* Views */
    private AppBarFragment appBarFrag;
    private EditText noteDescriptionBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);

        findViewById(R.id.nav_to_share_btn).setOnClickListener(this::navigateToShare);
        findViewById(R.id.delete_note_btn).setOnClickListener(this::showDeleteDialog);
        noteDescriptionBox = findViewById(R.id.note_content);
        appBarFrag = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);

        // Set the userApiKey for use throughout the class
        if (getSharedPreferences("user identification", Context.MODE_PRIVATE) != null) {
            setUserApiKey(getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", ""));
        }

        setJottingType("Note");
        if (getIntent().getBooleanExtra("Rename", false)) {
            displayRenameDialog((dialog, which) -> {
                TextView titleTextView = ((Dialog) dialog).findViewById(R.id.jot_name_edit);
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
        }
        else if (getIntent().hasExtra("data")) {
            setJottingData((Note) getIntent().getSerializableExtra("data"));
            assert getNoteData() != null;
            setJottingName(getNoteData().getName());
            setNoteBody(getNoteData().getBody());
        }

        noteDescriptionBox.setOnFocusChangeListener(this::onDetailsBoxFocus);
        findViewById(R.id.appbar).setOnLongClickListener(this::onRenameJotting);
    }

    protected void setJottingName(String name) {
        if (appBarFrag != null) appBarFrag.passTitle(name);
    }

    private void setNoteBody(String body) {
        noteDescriptionBox.setText(body == null || body.equals("null") ? "" : body);
    }

    protected void setLabels(ArrayList<Label> labels) {

    }

    /**
     * Convenience method for {@link JottingActivity#getJottingData()} with type Note
     * @return jottingData casted to a Note object
     */
    protected Note getNoteData() {
        return (Note) getJottingData();
    }

    private String getNoteBody() {
        return noteDescriptionBox.getText().toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("Rename", false)) return;

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
                        return this.connectToApi(encodeUrl("note", "id=" + getNoteData().getId()));
                    }
                }, data -> {
                    try {
                        if (data != null && data.has("data") && data.getInt("statusCode") >= 200 && data.getInt("statusCode") < 300) {
                            // If response ran okay
                            setNoteBody(data.getJSONObject("data").getString("body"));
                            setNoteBody(getNoteBody().replace("\\n", "\n"));
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
     * Calls {@link JottingActivity#displayRenameDialog(DialogInterface.OnClickListener)}
     * with "Note" as the second parameter
     */
    protected void displayRenameDialog(DialogInterface.OnClickListener onPositiveButtonClick) {
        super.displayRenameDialog(onPositiveButtonClick);
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
                        new DeleteJottingRequest(getNoteData().getId(), getUserApiKey(), "note"),
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

            if (!getNoteData().getBody().equals(getNoteBody())) {
                new TaskRunner().executeAsync(
                        new ApiRequestExecutor(getNoteBody()) {
                            @Override
                            protected void initialize() {
                                super.initialize();
                                setRequestMethod("PUT");
                                addRequestHeader("Authorization", "Basic " + getUserApiKey());
                                setQuery(encodePostQuery("body=%s"));
                            }

                            @Override
                            public JSONObject call() {
                                super.call();
                                return this.connectToApi(encodeUrl("note", "id=" + getNoteData().getId()));
                            }
                        }, data -> {
                            try {
                                if (data != null) {
                                    if (data.has("error"))
                                        Toast.makeText(this, data.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                                    else {
                                        getNoteData().setBody(getNoteBody());
                                        Log.d("body", getNoteData().getBody());
                                    }
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
