package com.boruminc.borumjot.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.PrivacyPolicyActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar(findViewById(R.id.my_toolbar));

        RecyclerView recyclerView = findViewById(R.id.home_jottings_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        JottingsListAdapter mAdapter = new JottingsListAdapter(new String[] {"Sample Task", "Sample Note", "Sample Note"}, this);
        recyclerView.setAdapter(mAdapter);

        Log.d("Adapter: ", String.valueOf(mAdapter.getItemCount()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.options_btn:
                navToOptions();
                return true;
            case R.id.helpandfdbck_btn:
                navToSupport();
                return true;
            case R.id.privacypolicy_btn:
                navToPrivacyPolicy();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void navToSupport() {
        startActivity(
                new Intent(
                        this,
                        HelpAndFeedbackActivity.class
                )
        );
    }

    private void navToOptions() {
        startActivity(
                new Intent(
                        this,
                        OptionsActivity.class
                )
        );
    }

    private void navToPrivacyPolicy() {
        startActivity(
                new Intent(
                        this,
                        PrivacyPolicyActivity.class
                )
        );
    }
}
