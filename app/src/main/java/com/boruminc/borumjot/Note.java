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
    public Note() {
        super();
    }

    public Note(String n) {
        super(n);
    }

    public Note(String n, String body, ArrayList<Label> labels) {
        super(n, body, labels);
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
