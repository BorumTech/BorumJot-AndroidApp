package com.boruminc.borumjot;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Jotting implements Serializable {
    ArrayList<Label> labels;
    private String name;

    Jotting(String n) {
        name = n;
        labels = new ArrayList<Label>();
    }

    Jotting(String n, ArrayList<Label> l) {
        name = n;
        labels = l;
    }

    public ArrayList<Label> getLabels() {
        return labels;
    }

    /**
     * Adds nLabel to labels list, if it isn't already in the list
     * @param nLabel The label to be appended
     */
    void addLabel(Label nLabel) {
        if (!labels.contains(nLabel)) {
            labels.add(nLabel);
        }
    }

    /**
     * Removes an existing label from the labels list, if it is in the list
     * @param oLabel The old label which already exists in the list
     * @return whether or not the label was removed
     */
    boolean removeLabel(Label oLabel) {
        return labels.remove(oLabel);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gives information about the jotting
     * @return the string representation of the Task, Note, or other Jotting type
     */
    @Override
    @NonNull
    public abstract String toString();
}
