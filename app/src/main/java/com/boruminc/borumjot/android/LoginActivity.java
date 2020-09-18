package com.boruminc.borumjot.android;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.boruminc.borumjot.ButtonGradient;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setSupportActionBar(findViewById(R.id.my_toolbar));

        // Set the title of the app bar
        AppNameAppBarFragment frag = (AppNameAppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        if (frag != null) frag.passTitle("Login");
    }

    @Override
    protected void onStart() {
        super.onStart();
        setGradientsBackgrounds();
    }

    public void setGradientsBackgrounds() {
        GradientDrawable gradient = ButtonGradient.getOneSelectButtonGradient();
        findViewById(R.id.login).setBackground(gradient);
        findViewById(R.id.registernavbtn).setBackground(gradient);
    }

    public void navToRegister(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void submitLogin(View view) {
        navToHome(view);
    }

    public void navToHome(View view) {
        // TODO Validation
        startActivity(new Intent(this, HomeActivity.class));
    }
}
