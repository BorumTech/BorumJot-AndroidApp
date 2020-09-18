package com.boruminc.borumjot.android;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OptionsActivity extends OptionsMenuItemActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options_activity);

        AppNameAppBarFragment frag = (AppNameAppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        if (frag != null) frag.passTitle("Options");
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

}
