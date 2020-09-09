package com.boruminc.borumjot.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

public class NoteActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);

        findViewById(R.id.nav_to_share_btn).setOnClickListener(this::navigateToShare);
        findViewById(R.id.delete_note_btn).setOnClickListener(this::showDeleteDialog);
    }

    public void navigateToShare(View view) {
        startActivity(new Intent(this, ShareActivity.class));
    }

    public void showDeleteDialog(View view) {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog
                .setTitle("Delete Note")
                .setMessage("Are you sure you would like to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO Delete
                    startActivity(new Intent(this, HomeActivity.class));
                    Toast.makeText(this, "The note was deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {

                });
        deleteDialog.create().show();
    }
}
