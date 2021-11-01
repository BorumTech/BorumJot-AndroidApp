package com.boruminc.borumjot;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.Date;

@RunWith(JUnit4.class)
public class TaskTest {
    private Task task;
    private String taskName;

    public TaskTest() {
        taskName = "My Sample Task";
        task = new Task(taskName);
    }

    @Test
    public void getName_isCorrect() {
        Assert.assertEquals("My Sample Task", taskName);
    }

    @Test
    public void userIdSetterAndGetter_areConnected() {
        int taskUserId = 10;
        task.setUserId(taskUserId);
        Assert.assertEquals(taskUserId, task.getUserId());
    }

    @Test
    public void timeCreatedGetterAndSetter_areConnected() {
        Date taskCreationTime = Date.valueOf("2020-10-06");
        task.setTimeCreated(taskCreationTime);
        Assert.assertEquals(taskCreationTime, task.getTimeCreated());

        taskCreationTime = Date.valueOf("2019-2-03");
        task.setTimeCreated(taskCreationTime);
        Assert.assertEquals(taskCreationTime, task.getTimeCreated());
    }
}
