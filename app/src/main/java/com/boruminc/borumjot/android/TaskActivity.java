package com.boruminc.borumjot.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import java.util.Objects;

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

                        });
                renameBuilder.create().show();
            } else {
                Toast.makeText(this, "Your phone is too old to name or rename the task. You can only rename on the website", Toast.LENGTH_LONG).show();
            }


        }

    }
}
