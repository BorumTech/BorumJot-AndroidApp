package com.boruminc.borumjot.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        AppBarFragment appBar = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);

        assert appBar != null;
        appBar.passTitle("Help");
    }

    /**
     * Opens the Meta Borum "Borum Jot" Topic page in user's browser
     * @param view The button that triggered this click event
     */
    public void onForumClick(View view) {
        Intent borumIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.forum_link)));
        startActivity(borumIntent);
    }

    public void onPrivacyPolicyClick(View view) {
        startActivity(new Intent(this, PrivacyPolicyActivity.class));
    }
}
