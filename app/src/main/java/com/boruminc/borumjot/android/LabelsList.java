package com.boruminc.borumjot.android;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LabelsList extends Fragment {
    private View root;
    private String userApiKey;

    private FlexboxLayoutManager labelLayout;
    private RecyclerView labelsRecycler;
    private LabelsListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        root = inflater.inflate(R.layout.labels_list, container, false);

        // Set the user api key
        // Get/set the user api key from the preferences because it is otherwise inaccessible
        userApiKey = requireActivity().getSharedPreferences("user identification", Context.MODE_PRIVATE)
                .getString("apiKey", "");

        labelLayout = new FlexboxLayoutManager(root.getContext());
        labelLayout.setJustifyContent(JustifyContent.FLEX_START);
        labelLayout.setAlignItems(AlignItems.CENTER);

        labelsRecycler = root.findViewById(R.id.labels_recycler);
        labelsRecycler.setLayoutManager(labelLayout);

        refreshLabelsList();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLabelsList();
    }

    private void refreshLabelsList() {
        new TaskRunner().executeAsync(labelsGetRequest, labelsGetResponse);
    }

    /* Labels */

    private ApiRequestExecutor labelsGetRequest = new ApiRequestExecutor() {
        @Override
        protected void initialize() {
            super.initialize();
            setRequestMethod("GET");
            addAuthorizationHeader(userApiKey);
        }

        @Override
        public JSONObject call() {
            super.call();
            return this.connectToApi(this.encodeQueryString("label"));
        }
    };

    private ApiResponseExecutor labelsGetResponse = new ApiResponseExecutor() {
        @Override
        public void onComplete(JSONObject result) {
            super.onComplete(result);
            try {
                if (ranOk()) {
                    ArrayList<Label> labelsData = JSONToModel.convertJSONToLabels(result.getJSONArray("data"), true);

                    adapter = new LabelsListAdapter(root.getContext(), labelsData);
                    labelsRecycler.setAdapter(adapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(root.getContext(), "An error occurred and the labels could not be fetched", Toast.LENGTH_LONG).show();
            }
        }
    };
}
