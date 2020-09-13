package com.boruminc.borumjot;

import androidx.annotation.NonNull;

public class Task extends Jotting {
    public Task(String n) {
        super(n);
    }

    @NonNull
    public String toString() {
        return "Name: " + getName();
    }
}
