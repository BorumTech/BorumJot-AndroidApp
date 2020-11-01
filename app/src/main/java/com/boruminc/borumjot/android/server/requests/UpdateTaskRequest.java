package com.boruminc.borumjot.android.server.requests;

import com.boruminc.borumjot.android.server.ApiRequestExecutor;

import org.json.JSONObject;

public class UpdateTaskRequest extends ApiRequestExecutor {
    private String userApiKey;
    private String[] getParameters;
    private String[] postParameters;

    /**
     * Constructor for UpdateTaskRequest
     * @param uApiKey The api key of the user who is making the subtask modification request
     * @param getParams The parameters to send in the query string: id=param1&name=param2
     * @param postParams The (encoded) parameters to send in the request body: body=param1
     */
    public UpdateTaskRequest(String uApiKey, String[] getParams, String[] postParams) {
        super(postParams);
        userApiKey = uApiKey;
        getParameters = getParams;
        postParameters = postParams;
    }

    @Override
    protected void initialize() {
        super.initialize();
        setRequestMethod("PUT");
        if (postParameters != null) setQuery(encodePostQuery("body=%s"));
        addRequestHeader("Authorization", "Basic " + userApiKey);
    }

    @Override
    public JSONObject call() {
        super.call();
        return this.connectToApi(encodeQueryString("task", getParameters));
    }
}
