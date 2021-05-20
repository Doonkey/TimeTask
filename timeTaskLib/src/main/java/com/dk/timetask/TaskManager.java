package com.dk.timetask;

import android.content.Context;

public class TaskManager<T extends Task> {

    public ITaskHandler<T> getTaskHandler(Context context){
        return new TaskHandler<>(context);
    }
}
