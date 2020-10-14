package com.boruminc.borumjot.android.server;

import com.boruminc.borumjot.Task;

import org.json.*;

import java.util.ArrayList;

public class JSONToModel {
    public static ArrayList<Task> convertJSONToTasks(JSONArray data) throws JSONException {
        ArrayList<Task> tasks = new ArrayList<Task>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject row = data.getJSONObject(i);
            tasks.add(new Task(row.getString("title")));
        }
        return tasks;
    }
}
