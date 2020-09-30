package com.boruminc.borumjot;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A type of jotting for unnotifiable entities
 * @author Varun Singh
 * @implSpec Can NOT send notifications about the content of the Note
 */
public class Note extends Jotting {
    private Set<String> sharees;

    public Note(String n) {
        super(n);
    }

    public Note(String n, String body, ArrayList<Label> labels) {
        super(n, body, labels);
        sharees = new HashSet<>();
    }

    /**
     * @return The value of the sharees PIV
     */
    public HashSet<String> getSharees() {
        return (HashSet<String>) sharees;
    }

    /**
     * Add a new person that will have access to this note
     * by adding the user to the sharees HashSet
     * @param newSharee The person who is requested to become a sharee for this note
     * @return Whether the new sharee was added
     */
    public boolean addSharee(String newSharee) {
        return sharees.add(newSharee);
    }

    @NonNull
    @Override
    public String toString() {
        return "Name: " + getName()
                + "\nShared with: " + getSharees();
    }
}
