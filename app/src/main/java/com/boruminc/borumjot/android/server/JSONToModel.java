package com.boruminc.borumjot.android.server;

import android.util.Log;

import com.boruminc.borumjot.Jotting;
import com.boruminc.borumjot.Label;
import com.boruminc.borumjot.Note;
import com.boruminc.borumjot.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

/**
 * Class for converting <code>JSONObject</code>s
 * to an object in the com.boruminc.borumjot package that models the data
 */
public class JSONToModel {
    public static ArrayList<Jotting> convertJSONToJottings(JSONArray jottingsData) throws JSONException {
        ArrayList<Jotting> jottings = new ArrayList<Jotting>();

        for (int i = 0; i < jottingsData.length(); i++) {
            JSONObject row = jottingsData.getJSONObject(i);

            if (row.getString("source").equals("note"))
                jottings.add(convertJSONToNote(row));
            else if (row.getString("source").equals("task")) {
                jottings.add(convertJSONToTask(row));
            }
        }

        return jottings;
    }

    /**
     * Converts a <code>JSONArray</code> of <code>JSONObject</code>s to a <code>Task</code> object
     * @param data The JSONArray
     * @return A list of Tasks corresponding to the JSON data
     * @throws JSONException if the id or title is not given for any row
     */
    public static ArrayList<Task> convertJSONToTasks(JSONArray data) throws JSONException {
        ArrayList<Task> tasks = new ArrayList<Task>();
        for (int i = 0; i < data.length(); i++) {
            tasks.add(convertJSONToTask(data.getJSONObject(i)));
        }

        return tasks;
    }

    /**
     * Converts a <code>JSONArray</code> of <code>JSONObject</code>s to a <code>Task</code> object
     * @param data The JSONArray
     * @return A list of Tasks corresponding to the JSON data
     * @throws JSONException if the id or title is not given for any row
     */
    public static ArrayList<Note> convertJSONToNotes(JSONArray data) throws JSONException {
        ArrayList<Note> notes = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            notes.add(convertJSONToNote(data.getJSONObject(i)));
        }

        return notes;
    }

    private static Task convertJSONToTask(JSONObject row) throws JSONException {
        // Set task information
        Task task = new Task(row.getString("title"), row.optString("body"), new ArrayList<Label>());
        task.setId(row.getInt("id"));
        task.setCompleted(row.optString("completed").equals("1"));
        task.setPriority(row.optInt("priority"));

        if (!row.isNull("due_date")) {
            Date dueDate = new Date(row.getLong("due_date") * 1000);
            task.setDueDate(dueDate);
        }

        return task;
    }

    private static Note convertJSONToNote(JSONObject row) throws JSONException {
        // Set note information
        Note note = new Note(row.getString("title"));
        note.setId(row.has("note_id") ? row.getInt("note_id") : row.getInt("id"));
        note.setPriority(row.optInt("priority"));

        return note;
    }

    public static ArrayList<Label> convertJSONToLabels(JSONArray data, boolean withAll) throws JSONException {
        ArrayList<Label> labels = new ArrayList<Label>();

        for (int i = 0; i < data.length(); i++) {
            if (withAll || data.getJSONObject(i).getBoolean("task_under_label"))
                labels.add(convertJSONToLabel(data.getJSONObject(i)));
        }

        return labels;
    }

    private static Label convertJSONToLabel(JSONObject data) throws JSONException {
        return new Label(
                data.getInt("id"),
                data.getString("name")
        );
    }

    /**
     * Converts JSONArray to HashSet of email <code>String</code>s
     * @param data The JSONArray
     * @return HashSet of Strings
     * @throws JSONException If the email property doesn't exist in any of the objects in the arrays
     */
    public static HashSet<String> convertJSONToUserEmails(JSONArray data) throws JSONException {
        HashSet<String> emails = new HashSet<>();

        for (int i = 0; i < data.length(); i++) {
            emails.add(data.getJSONObject(i).getString("email"));
        }

        return emails;
    }
}
