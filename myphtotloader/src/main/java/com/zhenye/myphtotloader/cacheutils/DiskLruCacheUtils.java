package com.zhenye.myphtotloader.cacheutils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class DiskLruCacheUtils {

    private static DiskLruCache myDiskLruCache;

    private static final String TAG = "DiskLruCacheUtils";
    private String imageUrl = null;
    private String imageKey = null;

    /**
     * 初始化DiskLruCache 和 DiskLruCache.Editor
     *
     * @param context    运行环境上下文，需要根据此参数获取应用程序本地缓存的位置
     * @param uniqueName 缓存文件夹名字。
     */
    public DiskLruCacheUtils(Context context, String uniqueName) {
        try {
            myDiskLruCache = openDiskLru(context, uniqueName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDiskLruCache(@NonNull String url,@NonNull Bitmap bitmap) {
        DiskLruCache.Editor myEditor;

        try {
            this.imageKey = hashKeyForCache.translated(url);
            this.imageUrl = url;
            myEditor = initDiskLruEditor(myDiskLruCache, this.imageKey);
            if (myEditor != null) {
                BufferedOutputStream bosEditor = new BufferedOutputStream(myEditor.newOutputStream(0));
                BufferedInputStream bis = new BufferedInputStream(BitmapToInputStream(bitmap));
                int b;
                while ((b = bis.read()) != -1) {
                    bosEditor.write(b);
                }
                if (bis != null) {
                    bis.close();
                }
                if (bosEditor != null) {
                    bosEditor.close();
                }
                Log.i(TAG, "数据存到本地成功");
                myEditor.commit();
            } else {
                Log.i(TAG, "数据存到本地失败，myEditor为空");
                if (myEditor != null) {
                    myEditor.abort();
                }
            }
            myDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public Bitmap getDiskLruCache(@NonNull String url) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot;

        try {
            imageKey = hashKeyForCache.translated(url);
            snapshot = myDiskLruCache.get(imageKey);
            if (snapshot != null) {
                bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(0));
                Log.i(TAG, "bitmap获取成功");
            } else {
                Log.i(TAG, "snap 为空");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    //打开缓存区
    @NonNull
    private DiskLruCache openDiskLru(@NonNull Context context,@NonNull String uniqueName) throws IOException {
        DiskLruCache diskLruCache;
        long MaxSize = 1024 * 1024 * 100;//分配磁盘内存为100M存图片
        diskLruCache = DiskLruCache.open(getDiskCacheDir(context, uniqueName), getAppVersion(context), 1, MaxSize);
        return diskLruCache;
    }

    //初始化editor
    @NonNull
    private DiskLruCache.Editor initDiskLruEditor(DiskLruCache diskLruCache, String ImageKey) throws IOException {
        return diskLruCache.edit(ImageKey);
    }

    /**
     * @param context
     * @param uniqueName
     * @return
     */
    //获取缓存地址
    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            Log.i(TAG, "有外部卡   ");

            cachePath = Objects.requireNonNull(context.getExternalCacheDir()).getPath();
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
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    //将bitmap转化为inputStream
    private InputStream BitmapToInputStream(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }


}
