package com.boruminc.borumjot.android.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageButton;

public class XButton extends AppCompatImageButton {
    Paint paint;

    public XButton(Context context) {
        super(context);
        paint = new Paint();
    }

    public XButton(Context context, AttributeSet attribute_set ) {
        super(context, attribute_set);
        paint = new Paint();
    }

    public XButton( Context context, AttributeSet attribute_set, int def_style_attribute ) {
        super(context, attribute_set, def_style_attribute);
        paint = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas) {
        float width = getMeasuredWidth();
        float height = getMeasuredHeight();
        paint.setColor(Color.BLACK);
        canvas.drawLine(0,0,width,height,paint);
        canvas.drawLine(width,0,0,height,paint);
    }
}