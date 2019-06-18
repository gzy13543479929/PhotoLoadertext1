package com.zhenye.photoloadertext1;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;
import com.zhenye.photoloadertext1.service.MyBindService1;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    String ImageUrl = "http://g.hiphotos.baidu.com/image/pic/item/6d81800a19d8bc3e770bd00d868ba61ea9d345f2.jpg";

    Button button1, button2;
    ImageView imageView;

//    DiskLruCacheUtils diskLruCacheUtils = new DiskLruCacheUtils();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.a1);
        button2 = findViewById(R.id.a2);
        imageView = findViewById(R.id.iv1);
        imageView.setImageResource(R.drawable.u1);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiskLruCache.Snapshot snapshot = null;
                try {
                    snapshot = DiskLruCacheUtils.myDiskLruCache.get(MD5code.hashKeyForDisk(ImageUrl));//單純本地讀取圖片。
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (snapshot != null) {
                    Log.i("11", "保存成功");
                    InputStream inputStream = snapshot.getInputStream(0);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bitmap);
                } else {
                }
                String key = MD5code.hashKeyForDisk(ImageUrl);
                Log.d("转换的密码是   ", key);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Log.d("MainActivity：", "你点击了返回键11");
                break;
            default:

        }
        return super.onKeyDown(keyCode, event);
    }


    public void hahaha(View view) {//按钮1的点击事件。
    }

    public void threadpoolTest(View view) {
        /**线程池测试*/
//        ThreadPoolExecutor threadPoolExecutor = ThreadPoolTest.getThreadPoolExecutor();
//        for(int i = 0;i<30;i++){
//            final int finali = i;
//            Runnable runnable = new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(2000);
//                        Log.d("Thread", "run: "+finali);
//                        Log.d("当前线程：",Thread.currentThread().getName());
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//            threadPoolExecutor.execute(runnable);
//    }
        /**stopService停止服务*/
//        Intent intent = new Intent(this, MyService1.class);
//        stopService(intent);
        if (isBound) {
            isBound = false;
            unbindService(connection);
//            stopService(intentService);
        }
    }

    public void delayT(View view) {

        /**startService开启服务*/
//        Intent intent = new Intent(this, MyService1.class);
//        startService(intent);
        intentService = new Intent(this, MyBindService1.class);
        bindService(intentService, connection, BIND_AUTO_CREATE);
    }

    Intent intentService;
    private boolean isBound = false;
    private MyBindService1.MyBinder binder;//靠binder执行binder的方法。
    private MyBindService1 bindService1 = null;//通过binder传实现对象过去，可以执行服务实例中的方法，不再局限于binder的方法。
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {//服务绑定成功时执行，服务被杀死时执行。
            isBound = true;
            binder = (MyBindService1.MyBinder) service;//获取连接的服务器对象的binder对象,可以用binder与service进行通信。
            bindService1 = binder.getService();//获取连接的服务器对象。
            MyBindService1.CallBack callBack = new MyBindService1.CallBack() {
                @Override
                public void setStr(String str) {//实例化回调函数。
                    Log.d(TAG, "我是在activity中的Log，接收到服务起发送的数据:" + str);
                }
            };
            bindService1.setCallBack(callBack);
            Log.d(TAG, "Activity onServiceConnected");
            int num = bindService1.gerRandomNumber();//然后就能使用服务中的方法了。
//            Log.d(TAG,"调用MyBindService1的方法gerRandomNumber"+num);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {//服务被杀掉
            isBound = false;
            Log.d(TAG, "Activity onServiceDisconnected");
        }
    };
}
