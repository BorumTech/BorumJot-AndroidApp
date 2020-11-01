package com.boruminc.borumjot.android.server.requests;

import com.boruminc.borumjot.android.server.ApiRequestExecutor;

import org.json.JSONObject;

public class DeleteJottingRequest extends ApiRequestExecutor {
    private String userApiKey;
    private String jottingType;

    public DeleteJottingRequest(int id, String uApiKey, String jotType) {
        super(String.valueOf(id));
        userApiKey = uApiKey;
        jottingType = jotType;
    }

    @Override
    protected void initialize() {
        super.initialize();
        setRequestMethod("DELETE");
        addRequestHeader("Authorization", "Basic " + userApiKey);
        setQuery(encodePostQuery("id=%s"));
    }

    @Override
    public JSONObject call() {
        super.call();
        return this.connectToApi(encodeQueryString(jottingType));
    }
}
