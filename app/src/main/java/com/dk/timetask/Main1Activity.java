package com.dk.timetask;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dk.timetask.timetask.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Main1Activity extends AppCompatActivity {
    TaskCallBack<ReserveTask> timeHandler = new TaskCallBack<ReserveTask>() {
        @Override
        public void taskExecute(ReserveTask task) {
            Log.e("TAG", "taskExecute" + task.toString());
        }

        @Override
        public void taskOverdue(ReserveTask task) {
            Log.e("TAG", "taskOverdue" + task.toString());
        }

        @Override
        public void taskFuture(ReserveTask task) {
            Log.e("TAG", "taskFuture" + task.toString());
        }
    };
    private ITaskHandler<ReserveTask> taskHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskHandler = new TaskManager<ReserveTask>().getTaskHandler(this);
        taskHandler.addCallBack(timeHandler);//添加任务回调
        taskHandler.addTask(createTasks());//把资源放进去处理
    }


    public void addTask(View view) {
        ReserveTask task_3 = new ReserveTask("task_3", dataOne("2021-05-20 10:53:00"), dataOne("2021-05-20 10:54:00"));
        taskHandler.addTask(task_3);
    }

    public void cancelTask(View view) {
        taskHandler.cancelTask("task_2");
    }

    private ReserveTask[] createTasks() {
        ReserveTask task_1 = new ReserveTask("task_1", dataOne("2021-05-20 09:00:00"), dataOne("2021-05-20 09:10:00"));
        ReserveTask task_2 = new ReserveTask("task_2", dataOne("2021-05-20 10:50:00"), dataOne("2021-05-20 10:51:00"));
        return new ReserveTask[]{task_1, task_2};
    }

    public static long dataOne(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date;
        String times = null;
        try {
            date = sdr.parse(time);
            long l = date.getTime();
            String stf = String.valueOf(l);
            times = stf.substring(0, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Long.parseLong(times) * 1000;
    }

    @Override
    protected void onDestroy() {
        taskHandler.release();
        super.onDestroy();
    }
}
