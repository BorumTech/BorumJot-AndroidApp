package com.boruminc.borumjot.android.subtasks;

import com.boruminc.borumjot.android.server.ApiRequestExecutor;
import com.boruminc.borumjot.android.server.Callback;
import com.boruminc.borumjot.android.server.TaskRunner;

import org.json.JSONObject;

public class SubtaskRetrieval {
    private int id;

    SubtaskRetrieval(int id) {
        this.id = id;
    }

    private ApiRequestExecutor makeSubtaskGetRequest(String userApiKey) {
        return new ApiRequestExecutor() {
            @Override
            protected void initialize() {
                super.initialize();
                setRequestMethod("GET");
                addAuthorizationHeader(userApiKey);
            }

            @Override
            public JSONObject call() {
                super.call();
                return this.connectToApi(encodeQueryString("task", String.format("id=%s", id)));
            }
        };
    }

    void runAsync(String userApiKey, Callback<JSONObject> callback) {
        new TaskRunner().executeAsync(makeSubtaskGetRequest(userApiKey), callback);
    }
}
