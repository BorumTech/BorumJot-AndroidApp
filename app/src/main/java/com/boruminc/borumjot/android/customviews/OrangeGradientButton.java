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
        setBackground(ButtonGradient.getOneSelectButtonGradient());
    }

    public OrangeGradientButton(Context context, AttributeSet attrs){
        super(context, attrs);
        setBackground(ButtonGradient.getOneSelectButtonGradient());
    }
}
