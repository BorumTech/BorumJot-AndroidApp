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
        initialize();
    }

    public XButton(Context context, AttributeSet attribute_set ) {
        super(context, attribute_set);
        initialize();
    }

    public XButton(Context context, AttributeSet attribute_set, int def_style_attribute ) {
        super(context, attribute_set, def_style_attribute);
        initialize();
    }

    private void initialize() {
        paint = new Paint();
        setBackground(null);
    }

    @Override
    public void onDraw(Canvas canvas) {
        float width = getMeasuredWidth();
        float height = getMeasuredHeight();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(4);
        canvas.drawLine(0,0,width,height,paint);
        canvas.drawLine(width,0,0,height,paint);
    }
}