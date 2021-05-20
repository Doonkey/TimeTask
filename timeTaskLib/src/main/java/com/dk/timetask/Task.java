package com.dk.timetask;


public abstract class Task {

    private String taskId;
    private long  starTime;
    private long  endTime;

    public Task(String taskId, long starTime, long endTime) {
        this.taskId = taskId;
        this.starTime = starTime;
        this.endTime = endTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getStarTime() {
        return starTime;
    }

    public void setStarTime(long starTime) {
        this.starTime = starTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", starTime=" + starTime +
                ", endTime=" + endTime +
                '}';
    }
}
