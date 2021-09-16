package com.boruminc.borumjot.android.labels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.JotLabel;
import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.android.R;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.google.android.material.checkbox.MaterialCheckBox;

import org.json.JSONObject;

import java.util.ArrayList;

public class JotLabelsListAdapter extends RecyclerView.Adapter<JotLabelsListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<JotLabel> labelData;
    private Jotting jotting;
    private String jotType;

    JotLabelsListAdapter(Context ct, ArrayList<JotLabel> d, Jotting j, String jt) {
        context = ct;
        labelData = d;
        jotting = j;
        jotType = jt;
    }

    @NonNull
    @Override
    public JotLabelsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.jot_label, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(JotLabelsListAdapter.ViewHolder holder, int position) {
        JotLabel labelBoolRelation = labelData.get(position);
        holder.name.setChecked(labelBoolRelation.getBelongs());
        holder.name.setText(labelBoolRelation.getLabel().getName());

        holder.name.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                jotting.addLabel(labelBoolRelation.getLabel());
            else
                jotting.removeLabel(labelBoolRelation.getLabel());

            new UpdateJottingLabels(jotting, jotType).runAsync(
                    context.getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("userApiKey", ""),
                    new ApiResponseExecutor() {
                        @Override
                        public void onComplete(JSONObject result) {
                            super.onComplete(result);
                            if (!ranOk()) {
                                Toast.makeText(context, "An error occurred and the label could not be added to this jotting", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
        });
    }

    @Override
    public int getItemCount() {
        return labelData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox name;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.label_name);
        }
    }
}