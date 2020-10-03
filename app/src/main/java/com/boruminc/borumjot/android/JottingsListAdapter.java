package com.boruminc.borumjot.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public final class JottingsListAdapter extends RecyclerView.Adapter<JottingsListAdapter.MyViewHolder> {
    private ArrayList<Jotting> mDataset;
    private Context context;

    ArrayList<Jotting> getDataset() {
        return mDataset;
    }

    void addItem(Jotting newJotting) {
        mDataset.add(newJotting);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private Context context;

        MyViewHolder(View v, Context c) {
            super(v);
            context = c;

            textView = v.findViewById(R.id.jotting_name);
            textView.setOnClickListener(this::navToJot);
            textView.setTextColor(Color.BLACK);

            // Surround each Jottings List item view with padding on all except the right side
            int padding = (int) context.getResources().getDimension(R.dimen.activity_horizontal_margin);
            textView.setPadding(padding, padding, 0, padding);
        }

        void bindView(Jotting jottingInst) {
            textView.setText(jottingInst.getName());
            textView.setTag(jottingInst);
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

            // Navigate to the proper Jotting type activity if data exists, otherwise, display error
            if (jottingIntent.hasExtra("data")) context.startActivity(jottingIntent);
            else Toast.makeText(context, "An error occurred in viewing your jotting", Toast.LENGTH_SHORT).show();
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
                .inflate(R.layout.my_text_view, parent, false);

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
