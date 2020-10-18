package com.boruminc.borumjot.android.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.io.Serializable;

public class SerializableImage extends AppCompatImageView implements Serializable {

    public SerializableImage(@NonNull Context context) {
        super(context);
    }

    public SerializableImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SerializableImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
