package com.zhenye.photoloadertext1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class huidiaoTest {

    //内部变量
    private static String TAG = "LoadImageUtils";
    private final int DOWNLOAD_IMAGE = 1, DOWNLOAD_BITMAP = 2;
    private ImageView LoadImageUtils_imageView1; //绑定组件
    private String LoadImageUtils_url; //访问的图片地址
    private Bitmap LoadImageUtils_bitmap; //获取的图片格式
    private LruCacheUtils lruCacheUtils = new LruCacheUtils();

    /**
     * @param imageView 绑定ImageView元素
     */
    public void LoadImage(ImageView imageView) {
//        Log.i(TAG, "LoadImage");
        LoadImageUtils_imageView1 = imageView;
    }

    /**
     * @param Url 传入图片地址
     */
    public void LoadUrl(String Url) {
//        Log.d(TAG, "LoadUrl");
        LoadImageUtils_url = Url;
    }

    /**
     * 从网上下载图片并在ImageView上更新
     */
    public void ShowImage() {
        Bitmap bitmap = GetImageFromLru(LoadImageUtils_url);
        if (bitmap == null) {
            Log.d(TAG, "缓存数据为空");
            new LoadingImageUtilsThread(LoadImageUtils_url).start();
        } else {
            Log.d(TAG, "从缓存中读数据");
            this.LoadImageUtils_imageView1.setImageBitmap(GetImageFromLru(LoadImageUtils_url));
        }
    }

    public void ShowImage(ImageView imageView, String Url) {
        LoadImage(imageView);
        LoadUrl(Url);
        Bitmap bitmap = GetImageFromLru(Url);
        if (bitmap == null) {
            Log.d(TAG, "缓存数据为空");
            new LoadingImageUtilsThread(Url).start();
        } else {
            Log.d(TAG, "从缓存中读数据");
            this.LoadImageUtils_imageView1.setImageBitmap(GetImageFromLru(Url));
        }
    }

    public static Bitmap SyncReturnImage(String Url) {
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
        } else {
            return null;
        }

    }

    /**
     * 缩放法压缩,例如将1080*720缩小成320*240，减小内存开销。
     * 将图片与ImageView进行匹配
     *
     * @param imageView 传入需要绑定的组件。
     * @param bitmap    传入需要缩放的bitmap
     *                  使用方法：new ZoomUtils().translactBitmap(imageView,bitmap);
     */
    private void TranslakeBitmap(ImageView imageView, Bitmap bitmap) {
        String TAG = "ZoomUtils";
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
            if (propotionX < propotionY) {
                propotion = propotionX;
            } else {
                propotion = propotionY;
            }
            Log.d(TAG, "缩放比" + (propotion));
            Log.d(TAG, "原图大小" + bitmap.getByteCount() / 1024);
            Matrix matrix = new Matrix();
            matrix.setScale(propotion, propotion);
            Bitmap bitmapReturn = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            Log.d(TAG, "缩小后" + bitmapReturn.getByteCount() / 1024);
            imageView.setImageBitmap(bitmapReturn);
        } else {
            Log.d(TAG, "bitmap传入错误");
        }
    }

    //开子线程下载图片，更新ImageView
    private class LoadingImageUtilsThread extends Thread {
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
    }

    //添加数据到缓存中
    private void PutImageToLru(String key, Bitmap bitmap) {
        lruCacheUtils.setLruCache(key, bitmap);
    }

    //从缓存中读数据
    private Bitmap GetImageFromLru(String key) {
        return lruCacheUtils.getLruCache(key);
    }

    //开Handler 处理数据，存入缓存
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_IMAGE:
                    //TODO 对获取到的图片进行操作。
                    Log.d(TAG, "图片获取完毕,更新imageView");
                    LoadImageUtils_bitmap = (Bitmap) msg.obj;
                    TranslakeBitmap(LoadImageUtils_imageView1, LoadImageUtils_bitmap);
                    PutImageToLru(LoadImageUtils_url, LoadImageUtils_bitmap);
                    break;

            }
        }
    };

}
