package com.boruminc.borumjot.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.*;
import com.boruminc.borumjot.android.customviews.SerializableImage;

import java.io.Serializable;
import java.util.ArrayList;

public final class JottingsListAdapter extends RecyclerView.Adapter<JottingsListAdapter.MyViewHolder> {

    private ArrayList<Jotting> mDataset;
    private Context context;

    ArrayList<Jotting> getDataset() {
        return mDataset;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private TextView textView;
        private SerializableImage pinIcon;
        private Context context;

        MyViewHolder(View v, Context c) {
            super(v);
            context = c;

            pinIcon = v.findViewById(R.id.pin_icon);
            pinIcon.setLongClickable(false);

            textView = v.findViewById(R.id.jotting_name);
            textView.setOnClickListener(this::navToJot);
            textView.setTextColor(Color.BLACK);

            pinIcon.setOnLongClickListener(this);
            textView.setOnLongClickListener(this);
            v.setOnLongClickListener(this);

            // Surround each Jottings List item view with padding on all except the right side
            int padding = (int) context.getResources().getDimension(R.dimen.activity_horizontal_margin);
            textView.setPadding(padding, padding, 0, padding);
        }

        void bindView(Jotting jottingInst) {
            textView.setText(jottingInst.getName());
            textView.setTag(jottingInst);
            pinIcon.setVisibility(jottingInst.getPriority() > 0 ? View.VISIBLE : View.INVISIBLE);
        }

        /**
         * UNDYNAMICALLY navigate to the right "Jotting" type activity.
         * Cannot be dynamic because AAB changes class names
         * @param v
         */
        void navToJot(View v) {
            // Create empty intent to the activity
            Intent jottingIntent = new Intent();

            // Set data and Class<?> destination for intent
            if (v.getTag() instanceof Task) {
                jottingIntent.setClass(context, TaskActivity.class);
                jottingIntent.putExtra("data", (Task) v.getTag());
            } else if (v.getTag() instanceof Note) {
                jottingIntent.setClass(context, NoteActivity.class);
                jottingIntent.putExtra("data", (Note) v.getTag());
            }

            if (jottingIntent.hasExtra("data")) {
                context.startActivity(jottingIntent);
                ((Activity) context).overridePendingTransition(0, 0);
            } // Navigate to the proper Jotting type activity if data exists, otherwise, display error
            else Toast.makeText(context, "An error occurred in viewing your jotting", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("Status", "Long clicked");
            AppCompatActivity currentActivity = (AppCompatActivity) context;
            Toolbar temporaryAppBar = currentActivity.findViewById(R.id.jotting_options_toolbar);

            // Set activity to properly display new action bar
            currentActivity.setSupportActionBar(temporaryAppBar);
            currentActivity.findViewById(R.id.my_toolbar).setVisibility(View.INVISIBLE);

            // Make visible and set data to jotting options toolbar
            temporaryAppBar.setVisibility(View.VISIBLE);

            Bundle bundle = new Bundle();
            bundle.putSerializable("data", (Serializable) v.getTag());
            bundle.putSerializable("view", pinIcon);

            ((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.jotting_options_toolbar).setArguments(bundle);

            return true;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    JottingsListAdapter(Context c) {
        context = c;
        mDataset = new ArrayList<Jotting>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public JottingsListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // Create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.jotting_list_item, parent, false);

        return new MyViewHolder(v, context);
    }

    /*
     * Replace the contents of a view (invoked by the layout manager)
     * Get element from the dataset at this position
     * Replace the contents of the view with that element
    */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bindView(mDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
