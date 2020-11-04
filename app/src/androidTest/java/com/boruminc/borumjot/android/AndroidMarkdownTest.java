package com.boruminc.borumjot.android;

import android.content.Context;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AndroidMarkdownTest {
    private AndroidMarkdown androidMarkdown;

    private Context context;

    AndroidMarkdownTest() {
        androidMarkdown = new AndroidMarkdown(new TextView(context));
    }

    public void getMarkdown_returnsStringRepresentation_of_spannableStringBuilder() {

    }

    public void formatRichTextView_formatsCorrectly() {

    }

    public void setHeadingSpannables_isCorrect() {

    }

    public void setFontSpannables_isCorrect() {

    }
}
