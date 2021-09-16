package com.boruminc.borumjot.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.android.labels.JotLabelsList;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.SlashNormalizer;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.boruminc.borumjot.android.server.requests.DeleteJottingRequest;
import com.boruminc.borumjot.android.server.requests.PinJottingRequest;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONObject;

public class NoteActivity extends JottingActivity {
    /* Views */
    private EditText noteDescriptionBox;
    private MaterialToolbar appBar;

    private Intent nextIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);

        appBar = findViewById(R.id.appbar);
        appBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.labels_btn:
                    onLabelListClick(item);
                    break;
                case R.id.share_btn:
                    navigateToShare();
                    break;
                case R.id.delete_btn:
                    showDeleteDialog();
                    break;
                case R.id.save_btn:
                    onSaveClick();
                    break;
                case R.id.pin_btn:
                    onPinClick(item);
                default:
                    return false;
            }

            return true;
        });

        noteDescriptionBox = findViewById(R.id.note_content);

        // Set the userApiKey for use throughout the class
        if (getSharedPreferences("user identification", Context.MODE_PRIVATE) != null) {
            setUserApiKey(getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("apiKey", ""));
        }

        setJottingType("Note");
        if (getIntent().getBooleanExtra("Rename", false)) {
            displayRenameDialog((dialog, which) -> {
                setJottingData(new Note());
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
                        return this.connectToApi(encodeQueryString("note"));
                    }
                }, new ApiResponseExecutor() {
                    @Override
                    public void onComplete(JSONObject result) {
                        super.onComplete(result);
                        if (this.ranOk()) {
                            getJottingData().setName(titleTextView.getText().toString());
                            try {
                                getJottingData().setId(result.getJSONObject("data").getInt("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(NoteActivity.this, "The note could not be created at this time. ",
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        } else if (getIntent().hasExtra("data")) {
            setJottingData((Note) getIntent().getSerializableExtra("data"));
            assert getNoteData() != null;
            setJottingName(getNoteData().getName());
            setNoteBody(getNoteData().getBody());
            setNotePriority(getNoteData().getPriority());

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            setLabelsList(new JotLabelsList());

            ft
                    .add(R.id.label_frame, getLabelsList())
                    .hide(getLabelsList())
                    .commit();

            Bundle b = new Bundle();
            b.putSerializable("jotting", getJottingData());
            b.putString("jotType", "note");

            getLabelsList().setArguments(b);
        }

        noteDescriptionBox.setOnFocusChangeListener(this::onDetailsBoxFocus);
        appBar.setOnLongClickListener(this::onRenameJotting);
    }

    private void setNotePriority(int priority) {
        if (priority == 1) {
            appBar.getMenu().findItem(R.id.pin_btn).setIcon(getResources().getDrawable(R.drawable.ic_filled_pin, getTheme()));
        }
    }

    private void onPinClick(MenuItem item) {
        new TaskRunner().executeAsync(new PinJottingRequest(getUserApiKey(), getJottingData()), new ApiResponseExecutor() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onComplete(JSONObject result) {
                super.onComplete(result);
                if (ranOk()) {
                    if (getJottingData().getPriority() == 0) {
                        getJottingData().setPriority(1);
                        item.setIcon(
                                getResources().getDrawable(R.drawable.ic_filled_pin, getTheme())
                        );
                    } else {
                        getJottingData().setPriority(0);
                        item.setIcon(
                                getResources().getDrawable(R.drawable.ic_outline_pin, getTheme())
                        );
                    }
                }
            }
        });
    }

    protected void setJottingName(String name) {
        ((TextView) findViewById(R.id.note_title)).setText(name);
    }

    private void setNoteBody(String body) {
        noteDescriptionBox.setText(body == null || body.equals("null") ? "" : body);
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
                        return this.connectToApi(encodeQueryString("note", "id=" + getNoteData().getId()));
                    }
                }, data -> {
                    try {
                        if (data != null && data.has("data") && data.getInt("statusCode") >= 200 && data.getInt("statusCode") < 300) {
                            // If response ran okay
                            String bodyToDisplay = data.getJSONObject("data").getString("body");
                            bodyToDisplay = SlashNormalizer.unescapeUserSlashes(bodyToDisplay);
                            setNoteBody(bodyToDisplay);
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
        if (nextIntent == null) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        nextIntent = null;
    }

    /**
     * Calls {@link JottingActivity#displayRenameDialog(DialogInterface.OnClickListener)}
     * with "Note" as the second parameter
     */
    protected void displayRenameDialog(DialogInterface.OnClickListener onPositiveButtonClick) {
        super.displayRenameDialog(onPositiveButtonClick);
    }

    public void navigateToShare() {
        nextIntent = new Intent(this, ShareActivity.class);
        nextIntent.putExtra("jotting", getNoteData());
        nextIntent.putExtra("jotType", "note");
        startActivity(nextIntent);
    }

    public void showDeleteDialog() {
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
        if (!isFocused) {
            if (!getNoteBody().equals(getNoteData().getBody())) {
                updateNoteBody(getNoteBody());
            }
        }
    }

    public void onSaveClick() {
        if (!getNoteBody().equals(getNoteData().getBody())) {
            updateNoteBody(getNoteBody());
        }
        startActivity(new Intent(this, HomeActivity.class));
    }

    public void updateNoteBody(String newBody) {
        new TaskRunner().executeAsync(getUpdateNoteBodyRequest(newBody), data -> {
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
        });
    }

    private ApiRequestExecutor getUpdateNoteBodyRequest(String newBody) {
        return new ApiRequestExecutor(newBody) {
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
                return this.connectToApi(encodeQueryString("note", "id=" + getNoteData().getId()));
            }
        };
    }
}
