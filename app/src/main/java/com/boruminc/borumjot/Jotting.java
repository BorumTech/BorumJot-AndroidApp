package com.boruminc.borumjot;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public abstract class Jotting implements Serializable, Comparable<Jotting> {
    private ArrayList<Label> labels;
    private String name;
    private String body;
    private int id;
    private int priority;
    private HashSet<String> sharees;

    Jotting() {
        name = "";
        body = "";
        labels = new ArrayList<Label>();
        priority = 0;
        sharees = new HashSet<>();
    }

    /**
     * Constructor for creating a new task
     * @param n The name
     */
    Jotting(String n) {
        name = n;
        labels = new ArrayList<Label>();
        body = "";
        priority = 0;
        sharees = new HashSet<>();
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
        sharees = new HashSet<>();
    }

    public ArrayList<Label> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<Label> newLabels) {
        labels = newLabels;
    }

    /**
     * Adds nLabel to labels list, if it isn't already in the list
     * @param nLabel The label to be appended
     */
    public void addLabel(Label nLabel) {
        if (!labels.contains(nLabel)) {
            labels.add(nLabel);
        }
    }

    /**
     * Removes an existing label from the labels list, if it is in the list
     * @param oLabel The old label which already exists in the list
     * @return whether or not the label was removed
     */
    public boolean removeLabel(Label oLabel) {
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
    public String toString() {
        return "Name: " + getName() + "\n"
                + "Priority: " + getPriority() + "\n"
                + "Sharees: " + getSharees().toString() + "";
    }

    @Override
    public boolean equals(Object jotting) {
        return super.equals(jotting) ||
                (id == ((Jotting) jotting).id
                        && this.getClass().equals(jotting.getClass())
                        && name.equals(((Jotting) jotting).getName())
                );
    }

    @Override
    public int compareTo(Jotting o) {
        return Integer.compare(-getPriority(), -o.getPriority());
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int newPriority) {
        priority = newPriority;
    }

    /**
     * @return The value of the sharees PIV
     */
    public HashSet<String> getSharees() {
        return sharees;
    }

    public void setSharees(HashSet<String> newSharees) {
        sharees = newSharees;
    }

    /**
     * Add a new person that will have access to this note
     * by adding the user to the sharees HashSet
     * @param newSharee The person who is requested to become a sharee for this note
     * @return Whether the new sharee was added
     */
    public void addSharee(String newSharee) {
        sharees.add(newSharee);
    }

    public boolean removeSharee(String existingSharee) {
        return sharees.remove(existingSharee);
    }
}
