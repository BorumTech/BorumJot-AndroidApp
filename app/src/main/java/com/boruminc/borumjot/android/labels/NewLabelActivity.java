package com.boruminc.borumjot.android.labels;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.boruminc.borumjot.android.R;

public class NewLabelActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_label);
    }

    public void onExitCreateLabelClick(View view) {
        finish();
    }
}
