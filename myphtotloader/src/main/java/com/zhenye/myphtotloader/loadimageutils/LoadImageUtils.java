package com.zhenye.myphtotloader.loadimageutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.zhenye.myphtotloader.cacheutils.DiskLruCacheUtils;
import com.zhenye.myphtotloader.cacheutils.LruCacheUtils;
import com.zhenye.myphtotloader.threadpoolutils.ThreadPoolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;

/**
 * author:gzy
 * imageView不能为warp_content格式
 * 使用方法：依次调用1.LruCache 2.LoadImage 3.LoadUrl 4.ShowImage（）
 * 例子：    LoadImageUtils loadImageUtils = new LoadImageUtils();
 * loadImageUtils.LoadUrl(ImageUrl);
 * loadImageUtils.LoadImage(imageView1);
 * loadImageUtils.ShowImage();
 * <p>
 * 另一种使用方法：调用ShowImage(ImageView imageView, String Url)
 * 例子：  LoadImageUtils loadImageUtils = new LoadImageUtils();
 * loadImageUtils.ShowImage(imageView1，ImageUrl);
 */
public class LoadImageUtils extends AbsLoadImageUtils {
    private static String TAG = "LoadImageUtils";
    private final int DOWNLOAD_IMAGE = 1;                  //常量

    private ImageView loadImageUtilsImageView1 = null;     //绑定要更新的imageView组件
    private String loadImageUtilsUrl = null;               //访问的图片地址

    private LruCacheUtils lruCacheUtils;
    private DiskLruCacheUtils diskLruCacheUtils;          //磁盘缓存类

    private ExecutorService executorService;

    public LoadImageUtils(Context context) {                //在图片工具类初始化的时候将内存缓存与本地缓存一起初始化
        String CACHE_FILENAME = "DiskLruCacheImage";
        diskLruCacheUtils = new DiskLruCacheUtils(context, CACHE_FILENAME);
        lruCacheUtils = new LruCacheUtils();               //内存缓存类
        executorService = ThreadPoolUtils.getCacheThreadPool();//获取无核心线程的线程池。
    }

    /**
     * @param imageView 绑定ImageView元素
     */
    @Override
    public void loadImage(ImageView imageView) {
//        Log.i(TAG, "LoadImage");
        loadImageUtilsImageView1 = imageView;
    }

    /**
     * @param Url 传入图片地址
     */
    @Override
    public void loadUrl(String Url) {
//        Log.d(TAG, "LoadUrl");
        loadImageUtilsUrl = Url;
    }

    /**
     * 从网上下载图片并在ImageView上更新
     */
    @Override
    public void showImage(ImageView imageView, String Url) {
        Bitmap loadImageUtilsBitmapCache;                   //缓存中读取出来的的bitmap

        loadImage(imageView);
        loadUrl(Url);
        loadImageUtilsBitmapCache = getImageFromLru(Url);

        if (loadImageUtilsBitmapCache == null) {
            Log.d(TAG, "缓存数据为空");
//            new LoadingImageUtilsThread(Url).start();
            executorService.execute(loadingImageUtilsThread(handler));
        } else {
            Log.d(TAG, "从缓存中读数据");
            this.loadImageUtilsImageView1.setImageBitmap(loadImageUtilsBitmapCache);
        }
    }

    @Nullable
    public static Bitmap syncReturnImage(@NonNull String Url) {        //同步方法类，不提供缓存到内存、本地功能。
        Bitmap bitmap = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(Url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
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

        if (bitmap != null) {
            return bitmap;
        }
        return null;
    }

    /**
     * 缩放法压缩,例如将1080*720缩小成320*240，减小内存开销。
     * 将图片与ImageView进行匹配
     *
     * @param imageView 传入需要绑定的组件。
     * @param bitmap    传入需要缩放的bitmap
     *                  使用方法：new ZoomUtils().translactBitmap(imageView,bitmap);
     */
    private Bitmap translateBitmap(ImageView imageView, Bitmap bitmap) {
        int imageViewIsWrapContent;
        Bitmap rbitmap = null;

        if (bitmap != null) {
            //获取组件宽、高
            float imageWidth, imageHeight, bitmapWidth, bitmapHeight;
            float propotionX, propotionY, propotion;
            imageWidth = (float) imageView.getWidth();
            imageHeight = (float) imageView.getHeight();
            bitmapWidth = (float) bitmap.getWidth();
            bitmapHeight = (float) bitmap.getHeight();
            propotionX = imageWidth / bitmapWidth;
            propotionY = imageHeight / bitmapHeight;
            imageViewIsWrapContent = (int) (imageHeight + imageWidth);

            if (imageViewIsWrapContent == 0) {
                Log.d(TAG, "error:属性不能设置为wrap_content");
                return null;
            }
            if (propotionX < propotionY) {
                propotion = propotionX;
            } else {
                propotion = propotionY;
            }

            Log.d(TAG, "缩放比" + (propotion));
            Log.d(TAG, "原图大小" + bitmap.getByteCount() / 1024);
            Matrix matrix = new Matrix();
            matrix.setScale(propotion, propotion);

            rbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            Log.d(TAG, "缩小后" + rbitmap.getByteCount() / 1024);
        } else {
            Log.d(TAG, "bitmap传入错误");
        }
        return rbitmap;
    }

    //更新ImageView
    private void renewImageView(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }


    private Runnable loadingImageUtilsThread(final Handler handler) {
        return new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run: " + Thread.currentThread().getName());
                Bitmap bitmap = null;
                HttpURLConnection connection = null;
                InputStream inputStream = null;

                try {
                    URL url = new URL(loadImageUtilsUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setRequestMethod("GET");
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        inputStream = connection.getInputStream();
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    }
                    handler.obtainMessage(DOWNLOAD_IMAGE, bitmap).sendToTarget();//从网络上获取Bitmap格式的图片。
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
        };
    }


    //添加数据到缓存中
    private void putImageToLru(String url, Bitmap bitmap) {
        lruCacheUtils.setLruCache(url, bitmap);
        diskLruCacheUtils.setDiskLruCache(url, bitmap);
    }

    //从缓存中读数据
    private Bitmap getImageFromLru(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        Bitmap bitmap_lruCache = lruCacheUtils.getLruCache(url);
        if (bitmap_lruCache != null) {
            return bitmap_lruCache;
        }

        Bitmap bitmap_diskLruCache = diskLruCacheUtils.getDiskLruCache(url);
        if (bitmap_diskLruCache != null) {
            return bitmap_diskLruCache;
        }

        return null;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {                  //开Handler 处理数据，存入缓存
        Bitmap loadImageUtilsBitmap;                        //压缩前的bitmap
        Bitmap loadImageUtilsBitmapTranslated = null;              //压缩后的bitmap

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_IMAGE:
                    //TODO 对获取到的图片进行操作。
                    Log.d(TAG, "图片获取完毕,更新imageView");
                    loadImageUtilsBitmap = (Bitmap) msg.obj;
                    if (loadImageUtilsImageView1 != null) {
                        loadImageUtilsBitmapTranslated = translateBitmap(loadImageUtilsImageView1, loadImageUtilsBitmap);
                        if (loadImageUtilsBitmapTranslated != null) {
                            putImageToLru(loadImageUtilsUrl, loadImageUtilsBitmapTranslated);//存照片进内存缓存和本地缓存。
                            renewImageView(loadImageUtilsImageView1, loadImageUtilsBitmapTranslated);//更新ImageView
                            loadImageUtilsBitmapTranslated = null;
                        } else {
                            break;
                        }
                    }
                    break;
                default:
                    Log.d(TAG, "handler default");
            }
        }
    };
}
