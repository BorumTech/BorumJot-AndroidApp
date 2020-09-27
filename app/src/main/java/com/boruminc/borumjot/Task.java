package com.boruminc.borumjot;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;

public class Task extends Jotting {
  private int id;
  private int userId;
  private int status;
  private Date timeCreated;

  public Task(JSONObject jsonObj) throws JSONException {
    super(jsonObj.getString("name"));
    id = jsonObj.getInt("id");
    userId = jsonObj.getInt("user_id");
    status = jsonObj.getInt("status");
    timeCreated = Date.valueOf(jsonObj.getString("time_created"));
  }

  public Task(String n) {
    super(n);
  }

  @NonNull
  public String toString() {
    return "Name: " + getName();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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
}
