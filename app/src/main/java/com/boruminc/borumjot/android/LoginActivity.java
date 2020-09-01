package com.boruminc.borumjot.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
    }

    public void navToRegister(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void submitLogin(View view) {
        startActivity(new Intent(this, HomeActivity.class));
    }
}