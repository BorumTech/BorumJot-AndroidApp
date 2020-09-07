package com.boruminc.borumjot.android;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

public class HelpAndFeedbackActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        LinearLayout ratingBar = findViewById(R.id.rating_bar);
        for (int i = 0; i < ratingBar.getChildCount(); i++) {
            int finalI = i;
            ratingBar.getChildAt(i).setOnClickListener(v -> {
                if (v.isSelected()) {
                    for (int j = 0; j < finalI; j++) {
                        v.setSelected(true);
                    }
                }

            });
        }
    }
}
