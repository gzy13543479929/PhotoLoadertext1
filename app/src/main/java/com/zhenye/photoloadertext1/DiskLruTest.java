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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DiskLruTest {

    private static DiskLruCache diskLruCache;
    private static DiskLruCache.Editor editor;
    private static long MaxSize = 1024 * 1024 * 100;//规定最大缓存容量。单位：字节,分配100m磁盘内存。
    private static String imageUrl;//图片地址
    private static String imagekey;//图片密码
    private static ImageView DiskLruCacheImageView;
    private static OutputStream DiskLruCacheoutputStream;
    private static InputStream DiskLruCacheinputStream;
    static String TAG = "DiskLruTest";
    static final int GETIMAGE = 1;

    //启动磁盘缓存的使用
    private static void DiskLruOpen(Context context, String uniqueName) throws IOException {
        diskLruCache = DiskLruCache.open(getDiskCacheDir(context, uniqueName), getAppVersion(context), 1, MaxSize);
    }

    //关闭磁盘缓存的使用
    private void DiskLruClose() throws IOException {
        if (diskLruCache != null) {
            diskLruCache.close();
        }
    }

    //存入待写入数据的key值
    private static void DiskLruEditor(String key) throws IOException {
        editor = diskLruCache.edit(imagekey);
    }

    //生成待写入数据到磁盘缓存的数据流
    public static void DiskLruSave(String url, Context context, String uniqueName, ImageView imageView) throws IOException {
        DiskLruOpen(context, uniqueName);//开启DiskLruCache；
        imageUrl = url;//将地址存入类中
        imagekey = hashKeyForDisk(imageUrl);//算出图片地址的密码。
        DiskLruEditor(imagekey);//绑定图片地址的密码。
        DiskLruCacheImageView = imageView;
        if (editor != null) {//如果绑定成功
            DiskLruCacheoutputStream = editor.newOutputStream(0);//editor包含key值。
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new LoadingImageUtilsThread(imageUrl);
                        editor.commit();

                        diskLruCache.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    //从磁盘缓存中取出来数据
    private static synchronized DiskLruCache.Snapshot getImageFromDiskLru() throws IOException {
        DiskLruCache.Snapshot snapShot = diskLruCache.get(imagekey);
        return snapShot;
    }

    public static void RenewImageView(String imageUrl, ImageView imageView) throws IOException {
        imagekey = hashKeyForDisk(imageUrl);
        DiskLruCache.Snapshot snapshot = diskLruCache.get(imagekey);
        if (snapshot != null) {
            InputStream is = snapshot.getInputStream(0);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);

        }
    }

    //删除缓存的元素
    public static synchronized boolean removeFromDiskLru(String imageUrl) throws IOException {
        try {
            String key = hashKeyForDisk(imageUrl);
            diskLruCache.remove(key);
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    //获取缓存地址
    private static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);//创建缓存目录
    }

    //获取应用程序版本号
    private static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    //对URL进行MD5编码。
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(key.getBytes());
            cacheKey = BYTEStOhEXsTRING(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
            e.printStackTrace();
        }
        return cacheKey;
    }

    private static String BYTEStOhEXsTRING(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    //开子线程下载图片，更新ImageView
    private static class LoadingImageUtilsThread extends Thread {
        private String Lurl;

        LoadingImageUtilsThread(String url) {
            this.Lurl = url;
        }

        @Override
        public void run() {
            Log.i(TAG, "run: " + Thread.currentThread().getName());
            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(Lurl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }
                handler.obtainMessage(GETIMAGE, bitmap).sendToTarget();//从网络上获取Bitmap格式的图片。
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    Log.i(TAG, "disconnect");
                    connection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GETIMAGE:
                    Log.d("AAA", "CG");
//                    BufferedInputStream in = new BufferedInputStream((InputStream)msg.obj);
//                    BufferedOutputStream out = new BufferedOutputStream(DiskLruCacheoutputStream);
//                    int b;
//                    while (true) {
//                        try {
//                            if (!((b = in.read()) != -1))
//                                out.write(b);
//                                break;
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    DiskLruCacheImageView.setImageBitmap((Bitmap) msg.obj);
                    break;
            }
        }
    };
}
