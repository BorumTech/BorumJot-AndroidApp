package com.boruminc.borumjot.android;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.MetricAffectingSpan;
import android.widget.TextView;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

public final class AndroidMarkdown {
    private TextView richView;
    private SpannableStringBuilder spannableStringBuilder;

    public AndroidMarkdown(TextView r) {
        richView = r;
        spannableStringBuilder = new SpannableStringBuilder(richView.getText().toString());
    }

    private String getMarkdown() {
        return spannableStringBuilder.toString();
    }

    public SpannableStringBuilder formatRichTextView() {
        setFontSpannables("**", Typeface.BOLD); // Sets the bold spannables
        setFontSpannables("__", Typeface.ITALIC); // Sets the italic spannables
        setHeadingSpannables(); // Sets the heading spannables (changes font size)
        return spannableStringBuilder;
    }

    /**
     * Sets heading 1
     */
    private void setHeadingSpannables() {
        new AbsoluteSizeSpan(25);
    }

    /* Helper Methods */

    private void setFontSpannables(String marker, int typeface) {
        if (!getMarkdown().contains(marker)) return;

        while (getMarkdown().contains(marker)) {
            int currMarkerFirstInd = getMarkdown().indexOf(marker);
            if (currMarkerFirstInd != getMarkdown().lastIndexOf(marker)) { // If there's more than one of marker

                spannableStringBuilder.setSpan(
                        new StyleSpan(typeface),
                        currMarkerFirstInd,
                        getMarkdown().indexOf(marker, currMarkerFirstInd + 1),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                spannableStringBuilder.delete(currMarkerFirstInd, currMarkerFirstInd + marker.length());
                spannableStringBuilder.delete(getMarkdown().indexOf(marker), getMarkdown().indexOf(marker) + marker.length());
            }
        }
    }
}
