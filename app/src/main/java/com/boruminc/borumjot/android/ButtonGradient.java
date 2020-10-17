package com.boruminc.borumjot.android;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class ButtonGradient {
  public static GradientDrawable getOneSelectButtonGradient() {
    return new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[] {
                    Color.parseColor("#F2D8A5"),
                    Color.parseColor("#F3C985"),
                    Color.parseColor("#F6AF4C"),
                    Color.parseColor("#F3C985"),
                    Color.parseColor("#F2D8A5"),
            }
    );
  }
}
