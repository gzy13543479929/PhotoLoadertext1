package com.zhenye.myphtotloader.cacheutils;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class LruCacheUtils {

    private static LruCache<String, Bitmap> lruCache;

    /**
     * 初始化LruCache
     * 缓存单位：kb
     */
    public LruCacheUtils() {
        long maxMemory = Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = (int) (maxMemory / 8);//将应用的1/8内存用作缓存区

        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;//每次存入图片都会调用此方法，此方法用于自动计算图片大小以及剩余缓存
            }
        };

    }

    /**
     * 存数据进内存缓存
     *
     * @param url    图片地址
     * @param bitmap 键值
     */
    public void setLruCache(String url, Bitmap bitmap) {
        String MD5_key = hashKeyForCache.translated(url);
        lruCache.put(MD5_key, bitmap);
    }

    /**
     * 从内存缓存中取数据
     *
     * @param url 图片地址
     * @return 图片的bitmap
     */
    public Bitmap getLruCache(String url) {
        String TAG = "LruCacheUtils";
        String MD5_key = hashKeyForCache.translated(url);
        Bitmap bitmap = lruCache.get(MD5_key);

        if (bitmap != null) {
            Log.i(TAG, "内存缓存中存在图片,返回图片");
            return bitmap;
        } else {
            Log.i(TAG, "内存缓存不存在图片,返回空");
            return null;
        }
    }

}
