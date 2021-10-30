package com.boruminc.borumjot;

import androidx.annotation.NonNull;

import java.sql.Date;
import java.util.ArrayList;

public class Task extends Jotting implements Comparable<Jotting> {
  private int userId;
  private Date timeCreated;
  private boolean completed;
  private ArrayList<Task> subtasks;
  private Date dueDate;
  private int parentId;

  public Task() {
    super();
    subtasks = new ArrayList<Task>();
    completed = false;
    parentId = 0;
  }

  public Task(String n) {
    super(n);
  }

  public Task(String n, String b, ArrayList<Label> labels) {
    super(n, b, labels);
  }

  public Task(String n, String b, ArrayList<Label> labels, int p) {
    super(n, b, labels);
    parentId = p;
  }

  @NonNull
  public String toString() {
    String str = super.toString();
    str += "Completed: " + completed;
    str += "Parent Id: " + parentId;

    return str;
  }

  @Override
  public int compareTo(Jotting o) {
    return Integer.compare(getPriority(), o.getPriority());
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public Date getTimeCreated() {
    return timeCreated;
  }

  void setTimeCreated(Date timeCreated) {
    this.timeCreated = timeCreated;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean isCompleted) {
    completed = isCompleted;
  }

  public ArrayList<Task> getSubtasks() {
    return subtasks;
  }

  public void setSubtasks(ArrayList<Task> newSubtasks) {
    subtasks = newSubtasks;
  }

  public void addSubtask(Task newSubtask) {
    subtasks.add(newSubtask);
  }

  public void addSubtask(int loc, Task newSubtask) {
    subtasks.add(loc, newSubtask);
  }

  public void setSubtask(int loc, Task subtask) {
    subtasks.set(loc, subtask);
  }

  public Date getDueDate() {
    return dueDate;
  }

  public void setDueDate(Date newDate) {
    dueDate = newDate;
  }

  public int getParentId() { return parentId; }

  public void setParentId(int newParentId) { parentId = newParentId; }
}
