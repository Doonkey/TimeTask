# 计划任务组件[![](https://jitpack.io/v/Doonkey/TimeTask.svg)](https://jitpack.io/#Doonkey/TimeTask)

根据系统闹钟的定时任务，按任务的执行时间顺序依次执行，可动态添加及取消任务。

不支持任务时间片重叠的场景

> 基于开源项目修改，感谢作者:[https://github.com/BolexLiu/TimeTask](https://github.com/BolexLiu/TimeTask)


### 使用说明

#### 导入依赖

Step 1. 添加JitPack仓库到根目录build.gradle文件

allprojects {

repositories {

...

maven { url '[https://jitpack.io](https://jitpack.io)' }

}

}

Step 2. 在module添加依赖[![](https://jitpack.io/v/Doonkey/TimeTask.svg)](https://jitpack.io/#Doonkey/TimeTask)

dependencies {

implementation 'com.github.Doonkey:TimeTask:${version}'

}

#### 初始化

其中的ReserveTask继承Task

   ```Java
   public class ReserveTask extends Task { 
     public ReserveTask(String taskId, long starTime, long endTime) {  
       super(taskId, starTime, endTime);  
     }  
   } 
   ```


   ```Java
   taskHandler = new TaskManager<ReserveTask>().getTaskHandler(this);
   taskHandler.addCallBack(timeHandler);//添加任务回调 
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
   ```


#### 添加任务

   ```Java
   taskHandler.addTask(ReserveTask... task);//把资源放进去处理
   ```


#### 取消任务

   ```Java
   taskHandler.cancelTask(String taskId);
   ```


#### 释放

 ```Java
 taskHandler.release();
 ```


