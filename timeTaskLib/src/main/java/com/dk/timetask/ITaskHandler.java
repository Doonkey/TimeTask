package com.dk.timetask;

public interface ITaskHandler<T extends Task> {
    void addCallBack(TaskCallBack<T> callBack);
    void addTask(T... task);
    void cancelTask(String taskId);
    void release();
}
