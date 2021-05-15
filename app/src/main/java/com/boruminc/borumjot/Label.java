package com.boruminc.borumjot;

import java.io.Serializable;

/**
 * The class that represents a label, which categorizes jottings (regardless of type) together
 * @author Varun Singh
 */
public class Label implements Serializable {
    private int id;
    private String name;
    private int userId;

    /**
     * Default constructor for Label object
     * @param id The id of the label
     * @param n The name or content of the label
     */
    public Label(int id, String n) {
        setId(id);
        setName(n);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || getId() == ((Label) obj).getId();
    }
}
