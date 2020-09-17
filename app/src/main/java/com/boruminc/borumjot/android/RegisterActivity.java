package com.boruminc.borumjot.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.boruminc.borumjot.ButtonGradient;

public class RegisterActivity extends AppCompatActivity {
    Button registerBtn;
    private AsyncTask<?, ?, ?> registerUserTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setSupportActionBar(findViewById(R.id.my_toolbar));

        this.navToPrivacyPolicy();

        registerBtn = findViewById(R.id.registerbtn);
        registerBtn.setEnabled(true);
        registerUserTask = new RegisterUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBtn.setBackground(ButtonGradient.getOneSelectButtonGradient());
    }

    public void validateRegistration(View view) {
        registerUserTask.execute();
        registerBtn.setEnabled(false); // Disable button because task can only be run once
    }

    private void navToPrivacyPolicy() {
        TextView promptConfirmPrivPolic = findViewById(R.id.confirm_priv_polic);
        ClickableSpan privPolicSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent toPrivacyPolicyIntent = new Intent(RegisterActivity.this, PrivacyPolicyActivity.class);
                toPrivacyPolicyIntent.putExtra("caller", "RegisterActivity");
                startActivity(toPrivacyPolicyIntent);
            }
        };
        String privPolicPrompt = promptConfirmPrivPolic.getText().toString();
        SpannableString confirmPrivPolicPrompt = new SpannableString(privPolicPrompt);
        int privacyPolicyInd = privPolicPrompt.indexOf("Privacy Policy");

        confirmPrivPolicPrompt.setSpan(privPolicSpan, privacyPolicyInd, privacyPolicyInd + "Privacy Policy".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        promptConfirmPrivPolic.setText(confirmPrivPolicPrompt);
        promptConfirmPrivPolic.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void toggle(View view) {
        ((CheckedTextView) view).toggle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel running task(s) to avoid memory leaks
        if (registerUserTask != null)
            registerUserTask.cancel(true);
    }
}
