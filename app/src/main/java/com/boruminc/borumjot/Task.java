package com.boruminc.borumjot;

import androidx.annotation.NonNull;

import java.sql.Date;
import java.util.ArrayList;

public class Task extends Jotting {
  private int userId;
  private int status;
  private Date timeCreated;
  private boolean completed;

  public Task(String n, String b, ArrayList<Label> labels) {
    super(n, b, labels);
  }

  @NonNull
  public String toString() {
    return "Name: " + getName();
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
}
