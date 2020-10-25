package com.boruminc.borumjot;

/**
 * The class that represents a label, which categorizes jottings (regardless of type) together
 * @author Varun Singh
 */
public class Label {
    private int id;
    private String name;
    private int userId;

    /**
     * Default constructor for Label object
     * @param n The name of the label
     * @param u The id of the user who made the label
     */
    public Label(int id, String n) {
        this.id = id;
        name = n;
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
