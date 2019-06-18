package com.zhenye.photoloadertext1;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * 调用了DiskLruTest中的MD5加密算法。
 */
public class LruCacheUtils {
    private static LruCache<String, Bitmap> lruCache;

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

    public void setLruCache(String key_value, Bitmap bitmap) {
        String key = DiskLruTest.hashKeyForDisk(key_value);
        lruCache.put(key, bitmap);
    }

    public Bitmap getLruCache(String key_value) {
        String key = DiskLruTest.hashKeyForDisk(key_value);
        Bitmap bitmap = lruCache.get(key);
        if (bitmap != null) {
            return bitmap;
        } else {
            return null;
        }
    }

}
