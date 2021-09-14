package com.boruminc.borumjot.android.labels;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.android.R;

import java.util.ArrayList;

public class LabelsListAdapter extends RecyclerView.Adapter<LabelsListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Label> labelData;

    LabelsListAdapter(Context ct, ArrayList<Label> d) {
        context = ct;
        labelData = d;
    }

    @NonNull
    @Override
    public LabelsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.labels_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LabelsListAdapter.ViewHolder holder, int position) {
        holder.name.setText(labelData.get(position).getName());
        holder.name.setOnClickListener(v -> {
            Intent label = new Intent(context, LabelActivity.class);
            label.putExtra("label", labelData.get(position));
            context.startActivity(label);
        });
    }

    @Override
    public int getItemCount() {
        return labelData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.label_name);
        }
    }
}