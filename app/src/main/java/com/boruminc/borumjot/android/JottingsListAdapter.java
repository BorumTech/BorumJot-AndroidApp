package com.boruminc.borumjot.android;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import androidx.core.content.ContextCompat;

public final class JottingsListAdapter extends RecyclerView.Adapter<JottingsListAdapter.MyViewHolder> {
    private String[] mDataset;
    private Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        private Context context;

        public MyViewHolder(View v, Context c) {
            super(v);
            textView = v.findViewById(R.id.jotting_name);
            textView.setOnClickListener(this::navToJot);
            context = c;
        }

        public void bindView(String string) {
            textView.setText(string);
        }

        public void navToJot(View v) {
            context.startActivity(new Intent(context, TaskActivity.class));
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public JottingsListAdapter(String[] myDataset, Context c) {
        mDataset = myDataset;
        context = c;
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
     * Get element from your dataset at this position
     * Replace the contents of the view with that element
    */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bindView(mDataset[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }

}
