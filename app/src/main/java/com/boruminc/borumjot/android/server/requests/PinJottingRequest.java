package com.boruminc.borumjot.android.server.requests;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.Task;
import com.boruminc.borumjot.android.server.ApiRequestExecutor;

import org.json.JSONObject;

public class PinJottingRequest extends ApiRequestExecutor {
    private String userApiKey;
    private Jotting jotData;

    public PinJottingRequest(String uKey, Jotting j) {
        userApiKey = uKey;
        jotData = j;
    }

    @Override
    protected void initialize() {
        super.initialize();
        setRequestMethod("PUT");
        addAuthorizationHeader(userApiKey);
    }

    @Override
    public JSONObject call() {
        super.call();

        String[] params = new String[2];
        params[0] = "id=" + jotData.getId();
        params[1] = "priority=" + (jotData.getPriority() == 0 ? 1 : 0);

        if (jotData instanceof Task) {
            return this.connectToApi(this.encodeQueryString("task", params));
        } else if (jotData instanceof Note) {
            return this.connectToApi(this.encodeQueryString("note", params));
        }

        return null;
    }
}
