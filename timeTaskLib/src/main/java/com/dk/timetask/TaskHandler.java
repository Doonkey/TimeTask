package com.dk.timetask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static android.content.Context.ALARM_SERVICE;

/**
 * 计划任务，按任务的执行时间顺序依次执行
 * 支持任务的添加/取消
 * 不支持任务时间片重叠的场景
 * @param <T> 任务
 */
class TaskHandler<T extends Task> implements ITaskHandler<T>{

    private final Context mContext;

    private final List<TaskCallBack<T>> mTaskCallBacks = new ArrayList<>();
    private final ArrayList<T> mTasks= new ArrayList<>();
    private final HashMap<String, PendingIntent> mDoTaskMap = new HashMap<>();
    public TaskHandler(Context context) {
        this.mContext = context.getApplicationContext();
        mContext.registerReceiver(taskReceiver, new IntentFilter("action.task"));
    }

    @Override
    public void addCallBack(TaskCallBack<T> callBack) {
        mTaskCallBacks.add(callBack);
    }

    /**
     * 添加任务 添加的任务按照任务的startTime先后顺序排序
     * @param tasks 任务
     */
    @SafeVarargs
    @Override
    public final void addTask(T... tasks){
        Collections.addAll(mTasks, tasks);
        //按照时间先后顺序排序
        Collections.sort(mTasks, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return Long.compare(o1.getStarTime(), o2.getStarTime());
            }
        });
        //当前有任务时先停止任务
        Iterator<String> taskIdsIterator = mDoTaskMap.keySet().iterator();
        if (taskIdsIterator.hasNext()){
            String taskId = taskIdsIterator.next();
            if (!mTasks.get(0).getTaskId().equals(taskId)) {
                cancelTask(taskId, false);
            }
        }else {
            nextTask();
        }
    }

    /**
     *从任务列表中中移除，若该任务已经添加闹钟，则取消当前正在进行的闹钟任务
     * @param taskId 任务id
     */
    @Override
    public void cancelTask(String taskId){
        cancelTask(taskId, true);
    }

    private synchronized void cancelTask(String taskId, boolean removeTask){
        if (removeTask){
            for (T mTask : mTasks) {
                if (taskId.equals(mTask.getTaskId())){
                    mTasks.remove(mTask);
                    break;
                }
            }
        }
        if (mDoTaskMap.size() > 0){
            AlarmManager manager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            try {
                Objects.requireNonNull(manager).cancel(mDoTaskMap.remove(taskId));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //进行下一个任务
            nextTask();
        }
    }
    @Override
    public void release(){
        try {
            mContext.unregisterReceiver(taskReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTaskCallBacks.clear();
        cancelAllTask();
    }

    private synchronized void nextTask(){
        if (mDoTaskMap.isEmpty()){
            start();
        }
    }

    private void start(){
        if (mTasks.size() > 0){
            T mTask = mTasks.get(0);
            long mNowTime = System.currentTimeMillis();
            //在当前区间内立即执行
            if (mTask.getStarTime() < mNowTime && mTask.getEndTime() > mNowTime) {
                for (TaskCallBack<T> taskCallBack : mTaskCallBacks) {
                    taskCallBack.taskExecute(mTask);
                }
                System.out.println("TimeTask=》当前时间:" + new Date(System.currentTimeMillis())+",执行时间:"+new Date(mTask.getStarTime()));
                configureAlarmManager(mTask, false);
                return;
            }
            //还未到来的消息 加入到定时任务
            if (mTask.getStarTime() > mNowTime) {
                for (TaskCallBack<T> taskCallBack : mTaskCallBacks) {
                    taskCallBack.taskFuture(mTask);
                }
                System.out.println("TimeTask=》当前时间:" + new Date(System.currentTimeMillis())+",预约时间:"+new Date(mTask.getStarTime()));
                configureAlarmManager(mTask, true);
                return;
            }
            //消息已过期
            if (mTask.getEndTime() < mNowTime) {
                for (TaskCallBack<T> taskCallBack : mTaskCallBacks) {
                    taskCallBack.taskOverdue(mTask);
                }
                System.out.println("TimeTask=》当前时间:" + new Date(System.currentTimeMillis())+",过期时间:"+new Date(mTask.getEndTime()));
                mDoTaskMap.clear();
                mTasks.remove(mTask);
            }
            //进行下一个任务
            nextTask();
        }
    }

    private void cancelAllTask(){
        mTasks.clear();
        if (mDoTaskMap.size() > 0){
            AlarmManager manager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            Iterator<PendingIntent> iterator = mDoTaskMap.values().iterator();
            try {
                while (iterator.hasNext()){
                    Objects.requireNonNull(manager).cancel(iterator.next());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mDoTaskMap.clear();
        }
    }

    private void configureAlarmManager(T task, boolean isStart) {
        try {
            AlarmManager manager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            PendingIntent pendIntent = getPendingIntent(task);
            long time = isStart ? task.getStarTime(): task.getEndTime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Objects.requireNonNull(manager).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Objects.requireNonNull(manager).setExact(AlarmManager.RTC_WAKEUP, time, pendIntent);
            } else {
                Objects.requireNonNull(manager).set(AlarmManager.RTC_WAKEUP, time, pendIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private PendingIntent getPendingIntent(T task) {
        int requestCode = 0;
        Intent intent = new Intent("action.task");
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mDoTaskMap.put(task.getTaskId(), mPendingIntent);
        return mPendingIntent;
    }

    public BroadcastReceiver taskReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //进行下一个任务
            start();
        }
    };
}
