package com.zhenye.photoloadertext1.inputoutputTest;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * 练习BufferInputStream/BufferOutputStream的使用。
 */
public class streamUtils {


    public static void inputStream(Context context, String uniqueName) {
        try {
            InputStream f = new FileInputStream(context.getCacheDir().getPath() + File.separator + "ssd.txt");
            String test;
            byte[] receivedbytes = new byte[2];
            byte[] rece = new byte[f.available()];
            f.read(rece);
            test = new String(rece);
            Log.d("我是接收到的数据", test);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputSream(Context context, String uniqueName) {
        Log.d("111", context.getCacheDir().getPath() + File.separator + "ssd.txt");
        try {
            OutputStream f = new FileOutputStream(context.getCacheDir().getPath() + File.separator + "ssd.txt");
            BufferedOutputStream bf = new BufferedOutputStream(f);
            String sendS = "我最帅你最美";
            f.write(sendS.getBytes());
            f.flush();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static File getFilePath(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);//创建缓存目录
    }


}
