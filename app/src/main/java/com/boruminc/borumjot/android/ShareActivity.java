package com.boruminc.borumjot.android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class ShareActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);

        ((AutoCompleteTextView) findViewById(R.id.share_email_field)).setAdapter(getSuggestions());
        ((Button) findViewById(R.id.delete_note_btn)).setOnClickListener(this::showDeleteDialog);
    }

    public ArrayAdapter<String> getSuggestions() {
        final String[] EMAILS = {"varunsingh87@protonmail.com", "barryallen@protonmail.com", "barrys87@yahoo.com", "billgates@gmail.com"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, EMAILS);

        return adapter;
    }

    public void showDeleteDialog(View view) {
        // TODO AlertDialog Builder
    }
}
