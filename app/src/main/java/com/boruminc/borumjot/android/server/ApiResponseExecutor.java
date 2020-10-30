package com.boruminc.borumjot.android.server;

import org.json.JSONObject;

public abstract class ApiResponseExecutor implements Callback<JSONObject> {
    private JSONObject result;

    @Override
    public void onComplete(JSONObject result) {
        this.result = result;
    }

    protected final boolean ranOk() {
        int statusCode = result.optInt("statusCode");
        return statusCode >= 200 && statusCode < 300;
    }
}
