package com.boruminc.borumjot.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

public class ChangeSignInActivity extends AppCompatActivity {
    AppBarFragment appBarFragment;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_sign_in_activity);
        WebView webView = findViewById(R.id.web_view);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        displayAppBar();

        webView.loadUrl("https://accounts.borumtech.com/account");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
    }

    private void displayAppBar() {
        appBarFragment = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        if (appBarFragment != null) {
            appBarFragment.passTitle("Borum Sphere Account");
        }
    }
}
