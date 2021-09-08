package com.boruminc.borumjot;

public final class JotLabel {
    private Label label;
    private boolean belongs;

    public JotLabel(Label l, boolean b) {
        label = l;
        belongs = b;
    }

    public Label getLabel() {
        return label;
    }

    public boolean getBelongs() {
        return belongs;
    }
}
