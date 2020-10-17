package com.boruminc.borumjot.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class OptionsActivity extends OptionsMenuItemActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options_activity);

        AppBarFragment frag = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        if (frag != null) frag.passTitle("Options");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void onLogoutClick(View view) {
        getSharedPreferences("user identification", Context.MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }
}
