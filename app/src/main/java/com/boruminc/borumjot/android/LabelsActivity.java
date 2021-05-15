package com.boruminc.borumjot.android;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LabelsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labels);

        AppBarFragment appBar = (AppBarFragment) getSupportFragmentManager().findFragmentById(R.id.appbar);
        assert appBar != null;
        appBar.passTitle("Labels");
    }
}
