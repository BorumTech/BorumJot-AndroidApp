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
public class Note extends Jotting implements Comparable<Jotting> {
    private HashSet<String> sharees;

    public Note(String n) {
        super(n);
        sharees = new HashSet<>();
    }

    public Note(String n, String body, ArrayList<Label> labels) {
        super(n, body, labels);
        sharees = new HashSet<>();
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

    @NonNull
    @Override
    public String toString() {
        return super.toString()
                + "\nShared with: " + getSharees();
    }

    @Override
    public int compareTo(Jotting o) {
        return getPriority() - o.getPriority();
    }
}
