package com.boruminc.borumjot.android;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.boruminc.borumjot.ButtonGradient;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setSupportActionBar(findViewById(R.id.my_toolbar));
        enableRegisterBtn();
    }

    private void enableRegisterBtn() {
        GradientDrawable gradient = ButtonGradient.getOneSelectButtonGradient();
        Button registerBtn = findViewById(R.id.registerbtn);

        registerBtn.setEnabled(true);
        registerBtn.setBackground(gradient);
    }
}
