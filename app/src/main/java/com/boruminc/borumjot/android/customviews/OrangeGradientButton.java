package com.boruminc.borumjot.android.customviews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.boruminc.borumjot.android.ButtonGradient;

public class OrangeGradientButton extends AppCompatButton {
    public OrangeGradientButton(@NonNull Context context) {
        super(context);
    }

    public OrangeGradientButton(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    public Drawable getBackground() {
        return ButtonGradient.getOneSelectButtonGradient();
    }
}
