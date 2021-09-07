package com.boruminc.borumjot.android.labels;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.android.R;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.google.android.material.checkbox.MaterialCheckBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JotLabelsListAdapter extends RecyclerView.Adapter<JotLabelsListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Label> labelData;
    private Jotting jotting;

    JotLabelsListAdapter(Context ct, ArrayList<Label> d, Jotting j) {
        context = ct;
        labelData = d;
        jotting = j;
    }

    @NonNull
    @Override
    public JotLabelsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.jot_label, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(JotLabelsListAdapter.ViewHolder holder, int position) {
        holder.name.setText(labelData.get(position).getName());
        holder.name.setOnClickListener(v -> {
            if (holder.editMode) {
                MaterialCheckBox labelCheckbox = (MaterialCheckBox) v;
                labelCheckbox.setChecked(!labelCheckbox.isChecked());

                if (labelCheckbox.isChecked())
                    jotting.addLabel(labelData.get(position));
                else
                    jotting.removeLabel(labelData.get(position));

                new UpdateJottingLabels(jotting, "note").runAsync(
                        context.getSharedPreferences("user identification", Context.MODE_PRIVATE).getString("userApiKey", ""),
                        new ApiResponseExecutor() {
                            @Override
                            public void onComplete(JSONObject result) {
                                super.onComplete(result);
                                try {
                                    labelData = JSONToModel.convertJSONToLabels(result.getJSONArray("data"), true);
                                } catch (JSONException e ) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
            } else {
                Intent label = new Intent(context, LabelActivity.class);
                label.putExtra("label", labelData.get(position));
                context.startActivity(label);
            }
        });
    }

    @Override
    public int getItemCount() {
        return labelData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox name;
        boolean editMode;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.label_name);
            editMode = false;
        }
    }
}