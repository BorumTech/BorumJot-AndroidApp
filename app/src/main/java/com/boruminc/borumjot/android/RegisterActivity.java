package com.boruminc.borumjot.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.boruminc.borumjot.ButtonGradient;
import com.boruminc.borumjot.android.validation.RegistrationValidation;

public class RegisterActivity extends AppCompatActivity {
    Button registerBtn;
    ProgressBar loadingSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setSupportActionBar(findViewById(R.id.my_toolbar));

        this.navToPrivacyPolicy();

        registerBtn = findViewById(R.id.registerbtn);
        registerBtn.setEnabled(true);

        loadingSpinner = findViewById(R.id.progressPanel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBtn.setBackground(ButtonGradient.getOneSelectButtonGradient());
        loadingSpinner.setVisibility(View.INVISIBLE);
    }

    public void validateRegistration(View view) {
        if (!((CheckedTextView) findViewById(R.id.confirm_priv_polic)).isChecked()) {
            Toast.makeText(this, "You must check the Privacy Policy checkbox to register", Toast.LENGTH_LONG).show();
            return;
        }

        RegistrationValidation validation = new RegistrationValidation(
                this,
                ((TextView) findViewById(R.id.first_name)).getText().toString(),
                ((TextView) findViewById(R.id.last_name)).getText().toString(),
                ((TextView) findViewById(R.id.email)).getText().toString(),
                ((TextView) findViewById(R.id.password)).getText().toString(),
                ((TextView) findViewById(R.id.confirm_password)).getText().toString()
        );

        String result = validation.validate();
        if (result.equals(RegistrationValidation.SUCCESS)) validation.checkRegistration(this);
        else Toast.makeText(this, result, Toast.LENGTH_LONG).show();
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


}
