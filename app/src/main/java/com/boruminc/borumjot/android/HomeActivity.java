package com.boruminc.borumjot.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
        inflater.inflate(R.menu.default_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.options:
                navToOptions();
                return true;
            case R.id.helpandfdbck:
                navToSupport();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void navToSupport() {

    }

    private void navToOptions() {

    }

}
