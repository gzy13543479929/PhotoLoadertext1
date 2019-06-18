package com.zhenye.photoloadertext1;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiskLruCacheUtils {

    public static DiskLruCache myDiskLruCache;
    private static DiskLruCache.Editor myEditor;
    private String imageKey;//图片地址。
    private String imageUrl;
    private static long MaxSize = 1024 * 1024 * 100;//规定最大缓存容量。单位：字节,分配100m磁盘内存。
    private String TAG = "DiskLruCacheUtils";
    private final int DOWNLOAD_IMAGE = 1;

    //初始化DiskLruCache
    public void DiskLruInit(Context context, String uniqueName, String url) {
        try {
            imageKey = MD5code.hashKeyForDisk(url);//算出密码
            imageUrl = url;
            myDiskLruCache = OpenDiskLru(context, uniqueName);//初始化磁盘缓存
            myEditor = DiskLruEditor(myDiskLruCache, imageKey);//初始化写入区。
            StartThread(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //打开缓存区
    private DiskLruCache OpenDiskLru(Context context, String uniqueName) throws IOException {
        DiskLruCache diskLruCache;
        diskLruCache = DiskLruCache.open(getDiskCacheDir(context, uniqueName), getAppVersion(context), 1, MaxSize);
        return diskLruCache;
    }

    //存入待写入数据的key值
    private DiskLruCache.Editor DiskLruEditor(DiskLruCache diskLruCache, String ImageKey) throws IOException {
        DiskLruCache.Editor editor = diskLruCache.edit(ImageKey);
        return editor;
    }

    /**********方法类***********/
    //获取缓存地址
    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            Log.i(TAG, "有外部卡   ");

            cachePath = context.getExternalCacheDir().getPath();
        } else {
            Log.i(TAG, "有内部卡   ");
            cachePath = context.getCacheDir().getPath();
        }
        Log.i(TAG, "本地地址    " + cachePath + File.separator + uniqueName);

        return new File(cachePath + File.separator + uniqueName + File.separator);//创建缓存目录
    }

    //获取应用程序版本号
    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.i(TAG, "版本号" + Integer.toString(info.versionCode));
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /***********图片下载相关线程操作***********/
    //开启子线程，下载图片，保存到了本地
    private void StartThread(final String Surl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    String SimageUrl = "https://img-my.csdn.net/uploads/201309/01/1378037235_7476.jpg";
//                    myEditor = myDiskLruCache.edit(imageKey);
                    if (myEditor != null) {
                        OutputStream outputStream = myEditor.newOutputStream(0);
                        if (downLoadUrlToStream(Surl, outputStream)) { //下载图片

                            Log.i(TAG, "网络成功");
                            myEditor.commit();


                        } else {
                            Log.i(TAG, "网络失败");
                            myEditor.abort();
                        }
                    }
                    myDiskLruCache.flush();

                    Log.i(TAG, "保存成功啦");
                    DiskLruCache.Snapshot snapshot = myDiskLruCache.get(imageKey);
                    if (snapshot == null) {
                        Log.i(TAG, "又失败啦");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //下载图片
    private boolean downLoadUrlToStream(String Lurl, OutputStream outputStream) {
        BufferedOutputStream bufferedOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        bufferedOutputStream = new BufferedOutputStream(outputStream, 8 * 1024);
        Log.i(TAG, "run: " + Thread.currentThread().getName());
        Bitmap bitmap = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(Lurl);
            connection = (HttpURLConnection) url.openConnection();
            bufferedInputStream = new BufferedInputStream(connection.getInputStream(), 8 * 1024);
            int b;
            while ((b = bufferedInputStream.read()) != -1) {
                bufferedOutputStream.write(b);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                Log.i(TAG, "disconnect");
                connection.disconnect();
            }
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
