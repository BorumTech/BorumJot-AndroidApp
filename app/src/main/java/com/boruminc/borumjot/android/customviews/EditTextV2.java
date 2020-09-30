package com.boruminc.borumjot.android.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import androidx.appcompat.widget.AppCompatEditText;

public class EditTextV2 extends AppCompatEditText {
    public EditTextV2( Context context ) {
        super(context);
    }

    public EditTextV2(Context context, AttributeSet attribute_set ) {
        super(context, attribute_set);
    }

    public EditTextV2( Context context, AttributeSet attribute_set, int def_style_attribute ) {
        super(context, attribute_set, def_style_attribute);
    }

    @Override
    public boolean onKeyPreIme( int key_code, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
            this.clearFocus();

        return super.onKeyPreIme(key_code, event);
    }
}
