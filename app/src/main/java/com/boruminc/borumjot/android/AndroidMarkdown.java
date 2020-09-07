package com.boruminc.borumjot.android;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

import java.lang.reflect.Type;

public final class AndroidMarkdown {
    private TextView richView;
    private SpannableStringBuilder spannableStringBuilder;

    public AndroidMarkdown(TextView r) {
        richView = r;
        spannableStringBuilder = new SpannableStringBuilder(getMarkdown());
    }

    public String formatRichTextView() {
        setBoldSpannables();
        setItalicSpannables();
        return spannableStringBuilder.toString();
    }

    private void setBoldSpannables() {
        setFontSpannables("**", Typeface.BOLD);
    }

    private void setItalicSpannables() {
        setFontSpannables("__", Typeface.ITALIC);
    }

    private String getMarkdown() {
        return richView.getText().toString();
    }

    /* Helper Methods */


    private void setFontSpannables(String marker, int typeface) {
        int markerFirstInd = getMarkdown().indexOf(marker);

        spannableStringBuilder.setSpan(
                new StyleSpan(typeface),
                markerFirstInd,
                getMarkdown().substring(
                        markerFirstInd + marker.length())
                        .indexOf(marker) + markerFirstInd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
    }
}
