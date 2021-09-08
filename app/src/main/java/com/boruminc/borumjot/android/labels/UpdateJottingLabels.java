package com.boruminc.borumjot.android.labels;

import android.util.Log;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.Callback;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONObject;

public class UpdateJottingLabels {
    private Jotting jotting;
    private String jottingType;

    public UpdateJottingLabels(Jotting jot, String jotType) {
        jotting = jot;
        jottingType = jotType;
    }

    /**
     * Returns the request that adds or removes labels from the current jotting
     *
     * @return The ApiRequestExecutor object
     */
    private ApiRequestExecutor makeRequest(String userApiKey) {
        StringBuilder labelIds = new StringBuilder();
        for (int i = 0; i < jotting.getLabels().size(); i++)
            labelIds.append(jotting.getLabels().get(i).getId()).append(",");

        return new ApiRequestExecutor(jottingType.toLowerCase(), String.valueOf(jotting.getId()), labelIds.toString()) {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("PUT");
                addAuthorizationHeader(userApiKey);
                setQuery(this.formatPostQuery("jot_type=%s&" + jottingType.toLowerCase() + "_id=%s&label_ids=%s"));
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString("labels"));
            }
        };
    }

    public void runAsync(String userApiKey, Callback<JSONObject> callback) {
        new TaskRunner().executeAsync(makeRequest(userApiKey), callback);
    }
}
