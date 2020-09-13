package com.boruminc.borumjot.android;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.fragment.app.FragmentActivity;

import java.util.Objects;

/**
 * super/subclass for activities that are navigated to through the HomeActivity's Options menu
 * Includes methods that override FragmentActivity methods that all OptionsItems inherit
 */
@SuppressLint("Registered")
public class OptionsMenuItemActivity extends FragmentActivity {
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
