package com.boruminc.borumjot.android;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;

public class PrivacyPolicyActivity extends OptionsMenuItemActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);

        AppBarFragment frag = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.activity_appbar);
        if (frag != null) frag.passTitle("Privacy Policy");

        WebView webView = findViewById(R.id.web_view);
        webView.loadUrl("https://jot.borumtech.com/legal/PrivacyPolicy/androidApp");
    }

}
