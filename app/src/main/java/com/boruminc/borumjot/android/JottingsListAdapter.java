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

public final class JottingsListAdapter extends RecyclerView.Adapter<JottingsListAdapter.MyViewHolder> {
    private ArrayList<Jotting> mDataset;
    private Context context;

    void setDataset(ArrayList<Jotting> dataset) {
        mDataset = dataset;
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

        void navToJot(View v) {
            try {
                // Creates intent to the activity that starts with the name of the tag instance's class
                Intent jottingIntent = new Intent(context, Class.forName(
                        "com.boruminc.borumjot.android." +
                        v.getTag().getClass().getSimpleName() +
                        "Activity"
                ));
                jottingIntent.putExtra("data", (Serializable) v.getTag());
                context.startActivity(jottingIntent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(context, "An error occurred in viewing your jotting", Toast.LENGTH_LONG).show();
            }
        }

    }
    JottingsListAdapter(Context c) {
        context = c;
        mDataset = new ArrayList<Jotting>();
    }

    // Provide a suitable constructor (depends on the kind of dataset)

    JottingsListAdapter(ArrayList<Jotting> myDataset, Context c) {
        context = c;
        mDataset = myDataset;
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
