package com.zhenye.myphtotloader.loadimageutils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * author:gzy
 * imageView不能为warp_content格式
 * 使用方法：依次调用1.LruCache 2.LoadImage 3.LoadUrl 4.ShowImage
 *例子：    LoadImageUtils loadImageUtils = new LoadImageUtils();
 *         loadImageUtils.LruInit();
 *         loadImageUtils.LoadUrl(ImageUrl);
 *         loadImageUtils.LoadImage(imageView1);
 *         loadImageUtils.ShowImage();
 */
public class LoadImageUtils extends AbsLoadImageUtils{
    //内部变量
    private static String TAG = "LoadImageUtils" ;
    private final int DOWNLOAD_IMAGE = 1,GET_BITMAP = 2;
    private ImageView LoadImageUtils_imageView1; //绑定组件
    private String LoadImageUtils_url; //访问的图片地址
    private Bitmap LoadImageUtils_bitmap ; //获取的图片格式
    private static LruCache<String,Bitmap> LoadImageUtils_LruCache;

    /**
     * @param imageView 绑定ImageView元素
     */
    @Override
    public void LoadImage(ImageView imageView) {
//        Log.i(TAG, "LoadImage");
        LoadImageUtils_imageView1 = imageView;
    }

    /**
     * @param Url 传入图片地址
     */
    @Override
    public void LoadUrl(String Url) {
//        Log.d(TAG, "LoadUrl");
        LoadImageUtils_url = Url;
    }

    /**
     * 从网上下载图片并在ImageView上更新
     */
    @Override
    public void ShowImage() {
        Bitmap bitmap = LoadImageUtils_LruCache.get(LoadImageUtils_url);
        if (bitmap ==null){
            Log.d(TAG,"缓存数据为空");
            new LoadingImageUtilsThread(LoadImageUtils_url).start();
        }else {
            Log.d(TAG,"从缓存中读数据");
            this.LoadImageUtils_imageView1.setImageBitmap(LoadImageUtils_LruCache.get(LoadImageUtils_url));
        }
    }

    /**
     *初始化缓存区
     */
    @Override
    public  void LruInit(){
        long maxMemory = Runtime.getRuntime().maxMemory()/1024;
        int cacheSize = (int) (maxMemory / 8);//将应用的1/8内存用作缓存区
        LoadImageUtils_LruCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key,Bitmap bitmap){
                return bitmap.getByteCount()/1024;//每次存入图片都会调用此方法，此方法用于计算图片大小以及剩余缓存
            }
        };
    }

    /**
     * 返回bitmap
     * @param Url
     */
    public Bitmap ReturnBitmap(String Url){
        Bitmap bitmap = null;
        if (Url != null){
            Log.i(TAG, "run: UI线程 ");
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
        }
        else {
            Log.d(TAG,"输入地址为空");
        }
        return bitmap;
    }

    /**
     * 缩放法压缩,例如将1080*720缩小成320*240，减小内存开销。
     * 将图片与ImageView进行匹配
     * @param imageView 传入需要绑定的组件。
     * @param bitmap 传入需要缩放的bitmap
     * 使用方法：new ZoomUtils().translactBitmap(imageView,bitmap);
     */
    private void translactBitmap(ImageView imageView, Bitmap bitmap) {
        String TAG = "ZoomUtils";
            if(bitmap != null){
                //获取组件宽、高
                float imageWidth,imageHeight,bitmapWidth,bitmapHeight;
                float propotionX,propotionY ,propotion;
                imageWidth =  (float) imageView.getWidth();
                imageHeight = (float) imageView.getHeight();
                bitmapWidth = (float) bitmap.getWidth();
                bitmapHeight =(float) bitmap.getHeight();
                propotionX = imageWidth/bitmapWidth;
                propotionY = imageHeight/bitmapHeight;
                if(propotionX<propotionY){
                    propotion = propotionX;
                }else {
                    propotion = propotionY;
                }
                Log.d(TAG,"缩放比"+(propotion));
                Log.d(TAG,"原图大小"+bitmap.getByteCount()/1024);
                Matrix matrix =new Matrix();
                matrix.setScale(propotion,propotion);
                Bitmap bitmapReturn = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                Log.d(TAG,"缩小后"+bitmapReturn.getByteCount()/1024);
                imageView.setImageBitmap(bitmapReturn);
            }else {
                Log.d(TAG,"bitmap传入错误");
            }
    }

    //开子线程下载图片，更新ImageView
    private class LoadingImageUtilsThread extends Thread{
        private String Lurl;
        LoadingImageUtilsThread(String url){
            this.Lurl = url;
        }
        @Override
        public void run(){
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
//                handler.obtainMessage(DOWNLOAD_IMAGE, inputStream).sendToTarget();//从网络上获取inputStream的图片。
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



    //开Handler 处理数据，存入缓存
    private  Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_IMAGE:
                    //TODO 对获取到的图片进行操作。
                    Log.d(TAG,"图片获取完毕");
                    LoadImageUtils_bitmap = (Bitmap)msg.obj;
//                    LoadImageUtils_imageView1.setImageBitmap(LoadImageUtils_bitmap);
                    translactBitmap(LoadImageUtils_imageView1,LoadImageUtils_bitmap);
                    PutImageToLru(LoadImageUtils_url,LoadImageUtils_bitmap);
                    break;
            }
        }
    };
    //添加数据到缓存中
    private void PutImageToLru(String key,Bitmap bitmap){
        Log.d(TAG,"将图片存入缓存");
        LoadImageUtils_LruCache.put(key,bitmap);
    }



}
