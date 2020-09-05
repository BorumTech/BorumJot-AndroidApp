package com.boruminc.borumjot;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.boruminc.borumjot.android.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PrivacyPolicyActivity extends FragmentActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);

        setPrivacyPolicyContent();
    }

    /**
     * Obtain the contents of the privacy_policy raw markdown resource using a BufferedReader
     * @return The full contents of the privacy policy file, or an empty string if the file or version is problematic
     */
    private String getPrivacyPolicyContent() {
        try (InputStream raw = getResources().openRawResource(R.raw.privacy_policy)){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                BufferedReader is = new BufferedReader(new InputStreamReader(raw, StandardCharsets.UTF_8));
                StringBuilder fileContents = new StringBuilder();
                String currentLine;
                while ((currentLine = is.readLine()) != null) {
                    fileContents.append(currentLine);
                }
                return fileContents.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d("Privacy Policy status", "Method finished");
        }

        return "";
    }

    private void setPrivacyPolicyContent() {
        TextView privacyPolicyContent = findViewById(R.id.privacy_policy_content);
        privacyPolicyContent.setText(getPrivacyPolicyContent());
    }
}
