package com.boruminc.borumjot.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

public class NoteActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);

        findViewById(R.id.nav_to_share_btn).setOnClickListener(this::navigateToShare);
    }

    public void navigateToShare(View view) {
        startActivity(new Intent(this, ShareActivity.class));
    }
}
