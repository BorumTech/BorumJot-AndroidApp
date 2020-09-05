package com.boruminc.borumjot.android;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

public class ShareActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_activity);

        ((AutoCompleteTextView) findViewById(R.id.share_email_field)).setAdapter(getSuggestions());

    }

    public ArrayAdapter<String> getSuggestions() {
        final String[] EMAILS = {"varunsingh87@protonmail.com", "barryallen@protonmail.com", "barrys87@yahoo.com", "billgates@gmail.com"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, EMAILS);

        return adapter;
    }


}
