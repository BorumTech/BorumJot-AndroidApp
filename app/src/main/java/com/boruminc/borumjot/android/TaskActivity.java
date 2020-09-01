package com.boruminc.borumjot.android;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class TaskActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity);
    }
}
