package com.boruminc.borumjot.android.server;

import com.boruminc.borumjot.*;
import org.json.*;

import java.util.ArrayList;

/**
 * Class for converting <code>JSONObject</code>s
 * to an object in the com.boruminc.borumjot package that models the data
 */
public class JSONToModel {
    /**
     * Converts a <code>JSONArray</code> of <code>JSONObject</code>s to a <code>Task</code> object
     * @param data The JSONArray
     * @return A list of Tasks corresponding to the JSON data
     * @throws JSONException if the id or title is not given for any row
     */
    public static ArrayList<Task> convertJSONToTasks(JSONArray data) throws JSONException {
        ArrayList<Task> tasks = new ArrayList<Task>();
        for (int i = 0; i < data.length(); i++) {
            Task row = convertJSONToTask(data.getJSONObject(i)); // Store JSON
            tasks.add(row);
        }
        return tasks;
    }

    public static Task convertJSONToTask(JSONObject row) throws JSONException {
        // Set task information
        Task task = new Task(row.getString("title"), row.optString("body"), new ArrayList<Label>());
        task.setId(row.getInt("id"));
        task.setCompleted(row.optString("completed").equals("1"));
        task.setPriority(row.optInt("priority"));

        return task;
    }

    public static Note convertJSONToNote(JSONObject row) throws JSONException {
        // Set note information
        Note note = new Note(row.getString("title"));
        note.setId(row.getInt("id"));
        note.setPriority(row.optInt("priority"));

        return note;
    }
}
