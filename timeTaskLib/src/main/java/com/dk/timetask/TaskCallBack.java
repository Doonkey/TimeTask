package com.dk.timetask;

public interface TaskCallBack<T extends Task> {

    void taskExecute(T task);
    void taskOverdue(T task);
    void taskFuture(T task);
}
