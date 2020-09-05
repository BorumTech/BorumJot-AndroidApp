package com.boruminc.borumjot.android;

import com.androidessence.lib.RichTextView;

public final class AndroidMarkdown {
    private RichTextView richTextView;

    public AndroidMarkdown(RichTextView r) {
        richTextView = r;
    }

    public void formatRichTextView() {
        setBoldSpannables();
        setItalicSpannables();
    }

    private void setBoldSpannables() {
        String boldMarker = "**";

        richTextView.formatSpan(
            getMarkdown().indexOf(boldMarker),
            getMarkdown().substring(
                    getMarkdown().indexOf(boldMarker) + 1)
                    .indexOf(boldMarker),
            RichTextView.FormatType.BOLD
        );
    }

    private void setItalicSpannables() {
        String italicMarker = "__";
        richTextView.formatSpan(
            getMarkdown().indexOf(italicMarker),
            getMarkdown().substring(getMarkdown().indexOf(italicMarker) + 1).indexOf(italicMarker),
            RichTextView.FormatType.ITALIC
        );
    }

    private String getMarkdown() {
        return richTextView.getText().toString();
    }
}
