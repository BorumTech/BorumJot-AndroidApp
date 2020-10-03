package com.boruminc.borumjot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Jotting implements Serializable {
    ArrayList<Label> labels;
    private String name;
    private String body;
    private int id;

    /**
     * Constructor for creating a new task
     * @param n The name
     */
    Jotting(String n) {
        name = n;
        labels = new ArrayList<Label>();
    }

    /**
     * Constructor for initializing an existing task
     * @param n The name
     * @param b The body
     * @param l The labels
     */
    Jotting(String n, String b, ArrayList<Label> l) {
        name = n;
        body = b;
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

    public String getBody() {
        return body;
    }

    public void setBody(String newBody) {
        body = newBody;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gives information about the jotting
     * @return the string representation of the Task, Note, or other Jotting type
     */
    @Override
    @NonNull
    public abstract String toString();

    @Override
    public boolean equals(Object jotting) {
        return super.equals(jotting) || (id == ((Jotting) jotting).id && this.getClass().equals(jotting.getClass()));
    }
}
