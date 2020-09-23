package com.boruminc.borumjot.android;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

/**
 * super/subclass for activities that are navigated to through the HomeActivity's Options menu
 * Includes methods that override FragmentActivity methods that all OptionsItems inherit
 */
public abstract class OptionsMenuItemActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(findViewById(R.id.my_toolbar));
    }

    /*
     * When the user presses the back button, this function gets invoked automatically.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (getIntent().hasExtra("caller") && Objects.equals(getIntent().getStringExtra("caller"), "RegisterActivity"))
                return;
        }

        overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);

    }
}
