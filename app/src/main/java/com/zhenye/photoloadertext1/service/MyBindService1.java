package com.zhenye.photoloadertext1.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

public class MyBindService1 extends Service {

    String TAG = "MyBindService1";

    public MyBindService1() {

    }

    @Override
    public void onCreate() {
        Log.d(TAG, "  onCreate is implement");
        super.onCreate();
    }

    String a = "我是从服务来的数据";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "  onStartCommand is implement");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "  onDestroy is implement");
        if (callBack != null) {
            callBack.setStr(a);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "  onBind is implement");
        // TODO: Return the communication channel to the service.
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "  onUnbind is implement");
        return super.onUnbind(intent);
    }

    public class MyBinder extends Binder { //activity可以执行此类中的方法。

        public MyBindService1 getService() {
            return MyBindService1.this;
        }
    }

    private MyBinder binder = new MyBinder();
    private Random generator = new Random();

    public int gerRandomNumber() {
        return generator.nextInt();
    }

    /**
     * 回调函数的使用：
     * 1.定义接口以及接口方法，接口方法包含需要传递的变量类型
     * 2.实例化接口
     * 3.建立函数setCallBack,将外部实例化接口方法传递进来。
     * 4.在本例中调用回调函数。
     */
    CallBack callBack = null;

    public interface CallBack {
        void setStr(String str);//此方法在外部实现
    }

    public void setCallBack(CallBack callBack) {//此方法用于将外部实现与本类进行绑定。
        this.callBack = callBack;
    }

    public CallBack getCallBack() {
        return callBack;
    }
}
