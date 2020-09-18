package com.boruminc.borumjot.android;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PrivacyPolicyActivity extends OptionsMenuItemActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);
        setPrivacyPolicyContent();

        AppNameAppBarFragment frag = (AppNameAppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        if (frag != null) frag.passTitle("Privacy Policy");
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
        TextView privacyPolicyContent = findViewById(R.id.privacy_policy_content);
        privacyPolicyContent.setText(getPrivacyPolicyContent());

        // Display as markdown
        AndroidMarkdown contentInMarkdown = new AndroidMarkdown(privacyPolicyContent);

        new SpannableStringBuilder(getPrivacyPolicyContent()).setSpan(
                new StyleSpan(Typeface.BOLD),
                getPrivacyPolicyContent().indexOf("**"),
                getPrivacyPolicyContent().indexOf("**", getPrivacyPolicyContent().indexOf("**") + 1),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        privacyPolicyContent.setText(contentInMarkdown.formatRichTextView());

    }
}
