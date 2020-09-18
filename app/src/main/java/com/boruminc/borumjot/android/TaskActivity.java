package com.boruminc.borumjot.android;

import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class TaskActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getBooleanExtra("Rename", true)) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AlertDialog.Builder renameBuilder = new AlertDialog.Builder(this);
                renameBuilder
                        .setView(getLayoutInflater().inflate(R.layout.rename_task_dialog, null))
                        .setTitle("Task Name")
                        .setCancelable(true)
                        .setOnCancelListener(dialog -> finish())
                        .setPositiveButton("Save", (dialog, which) -> {
                            TextView titleTextView = ((Dialog) dialog).findViewById(R.id.task_name_edit);

                            if (titleTextView == null) { // Display erorr and exit if appbar title could not be found
                                Toast.makeText(this, "An error occured and you cannot name the task at this time. Try on the web or desktop apps.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            AppNameAppBarFragment frag = (AppNameAppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
                            if (frag != null) frag.passTitle(titleTextView.getText().toString());
                        });
                renameBuilder.create().show();
            } else {
                Toast.makeText(this, "Your phone is too old to name or rename the task. You can only rename on the website", Toast.LENGTH_LONG).show();
            }


        }

    }
}
