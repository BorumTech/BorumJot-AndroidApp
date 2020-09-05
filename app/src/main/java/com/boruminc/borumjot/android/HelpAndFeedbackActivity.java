package com.boruminc.borumjot.android;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
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
                if (((CheckBox) v).isChecked()) {
                    for (int j = 0; j < finalI; j++) {
                        ((CheckBox) v).setChecked(true);
                    }
                }

            });
            Log.d("First view is checked", String.valueOf(((CheckBox) ratingBar.getChildAt(0)).isChecked()));
        }
    }
}
