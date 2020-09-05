package com.boruminc.borumjot;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.androidessence.lib.RichTextView;
import com.boruminc.borumjot.android.AndroidMarkdown;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                BufferedReader is = new BufferedReader(new InputStreamReader(raw, StandardCharsets.UTF_8));
                StringBuilder fileContents = new StringBuilder();
                String currentLine;
                while ((currentLine = is.readLine()) != null) {
                    fileContents.append(currentLine).append("\n");
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
        // Retrieve RichTextView and content as plaintext
        RichTextView privacyPolicyContent = findViewById(R.id.privacy_policy_content);
        privacyPolicyContent.setText(getPrivacyPolicyContent());

        // Display as markdown
        AndroidMarkdown contentInMarkdown = new AndroidMarkdown(privacyPolicyContent);
        contentInMarkdown.formatRichTextView();
    }
}
