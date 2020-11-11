package com.boruminc.borumjot;

import androidx.annotation.NonNull;

import java.sql.Date;
import java.util.ArrayList;

public class Task extends Jotting implements Comparable<Jotting> {
  private int userId;
  private int status;
  private Date timeCreated;
  private boolean completed;
  private ArrayList<Task> subtasks;

  public Task() {
    super();
  }

  public Task(String n) {
    super(n);
  }

  public Task(String n, String b, ArrayList<Label> labels) {
    super(n, b, labels);
  }

  @NonNull
  public String toString() {
    return super.toString();
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

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public Date getTimeCreated() {
    return timeCreated;
  }

  public void setTimeCreated(Date timeCreated) {
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
}
