package com.boruminc.borumjot.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.boruminc.borumjot.ButtonGradient;
import com.boruminc.borumjot.android.validation.LoginValidation;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the user is already logged in, immediately redirect to HomeActivity and erase this from backstack
        if (!getSharedPreferences("user identification", Context.MODE_PRIVATE)
                .getString("apiKey", "")
                .equals("")
        ) {
            finish();
            Intent homeIntent = new Intent(this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            return;
        }

        setContentView(R.layout.activity_login);
        setSupportActionBar(findViewById(R.id.my_toolbar));

        // Set the title of the app bar
        AppNameAppBarFragment frag = (AppNameAppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        if (frag != null) frag.passTitle("Login");
    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.progressPanel).setVisibility(View.INVISIBLE);
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
        LoginValidation validation = new LoginValidation(
            this,
            ((TextView) findViewById(R.id.username)).getText().toString(),
            ((TextView) findViewById(R.id.password)).getText().toString()
        );

        String resultText = validation.validate();

        if (resultText.equals(LoginValidation.SUCCESS)) validation.checkLogin();
        else Toast.makeText(this, resultText, Toast.LENGTH_LONG).show();
    }

    public void navToHome(View view) {
        startActivity(new Intent(this, HomeActivity.class));
    }
}
