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
        appBar.passTitle("Help and Feedback");
    }

    /**
     * Opens the Meta Borum "Borum Jot" Topic page in user's browser
     * @param view The button that triggered this click event
     */
    public void onForumClick(View view) {
        Intent forumIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.forum_link)));
        startActivity(forumIntent);
    }

    public void onPrivacyPolicyClick(View view) {
        startActivity(new Intent(this, PrivacyPolicyActivity.class));
    }

    public void onContactClick(View view) {
        Uri uri = Uri.fromParts(
                "mailto",
                getString(R.string.support_email),
                null
        );
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri)
                .putExtra(Intent.EXTRA_SUBJECT, "Feedback for Borum Jot");

        startActivity(intent);
    }

    public void onTwitterClick(View view) {
        Intent twitterIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.twitter_link)));
        view.requestFocus();
        startActivity(twitterIntent);
    }

    public void onProductHuntClick(View view) {
        Intent productHuntIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.product_hunt_link)));
        startActivity(productHuntIntent);
    }
}
