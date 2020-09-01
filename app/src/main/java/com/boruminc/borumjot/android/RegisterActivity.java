package com.boruminc.borumjot.android;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
    }

    private void enableRegisterBtn() {
        findViewById(R.id.registerbtn).setEnabled(true);
    }
}
