package com.boruminc.borumjot.android.labels;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.boruminc.borumjot.JotLabel;
import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.android.R;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.ApiResponseExecutor;
import com.boruminc.borumjot.android.server.JSONToModel;
import com.boruminc.borumjot.android.server.TaskRunner;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class JotLabelsList extends Fragment {
    private View root;
    private String userApiKey;

    private FlexboxLayoutManager labelLayout;
    private RecyclerView labelsRecycler;
    private JotLabelsListAdapter adapter;

    private Jotting jotting;
    private String jotType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        root = inflater.inflate(R.layout.labels_list, container, false);

        // Set the user api key
        // Get/set the user api key from the preferences because it is otherwise inaccessible
        userApiKey = requireActivity().getSharedPreferences("user identification", Context.MODE_PRIVATE)
                .getString("apiKey", "");

        Bundle bundle = getArguments();
        assert bundle != null;
        jotting = (Jotting) bundle.getSerializable("jotting");
        jotType = bundle.getString("jotType");

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
            return this.connectToApi(this.encodeQueryString("labels", "jot_type=" + jotType, "id=" + jotting.getId()));
        }
    };

    private ApiResponseExecutor labelsGetResponse = new ApiResponseExecutor() {
        @Override
        public void onComplete(JSONObject result) {
            super.onComplete(result);
            try {
                if (ranOk()) {
                    ArrayList<JotLabel> labelsData = JSONToModel.convertJSONToLabelBooleanMap(result.getJSONArray("data"));
                    adapter = new JotLabelsListAdapter(root.getContext(), labelsData, jotting);
                    labelsRecycler.setAdapter(adapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(root.getContext(), "An error occurred and the labels could not be fetched", Toast.LENGTH_LONG).show();
            }
        }
    };
}
