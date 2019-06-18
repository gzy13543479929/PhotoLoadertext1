package com.zhenye.photoloadertext1.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService1 extends Service {
    String TAG = "MyService1";

    public MyService1() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "  onCreate is implement");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "  onStartCommand is implement");
        stopSelf();//执行任务完毕，关闭服务。
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "  onDestory is implement");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "  onBind is implement");
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
