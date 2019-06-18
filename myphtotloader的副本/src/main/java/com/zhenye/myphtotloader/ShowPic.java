package com.zhenye.myphtotloader;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

public class ShowPic {
    static final String TAG = "ShowPic";
    /**
     *
      * @param url 图片地址
     * @param listener  图片请求成功的回调
     * @param maxWidth 允许图片最大的宽度  大于最大值就会进行压缩，指定为0则全部都不会压缩
     * @param maxHeight 允许图片最大的高度
     * @param decodeConfig 指定图片颜色属性
     * @param errorListener 图片请求失败的回调。可以在这里放一张默认图片。
     */
    public static void showPhoto_net_synchronization(
            String url, Response.Listener<Bitmap> listener,
            int maxWidth, int maxHeight, Bitmap.Config decodeConfig,
            Response.ErrorListener errorListener){
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        // TODO Auto-generated method stub
                    }
                }, maxWidth, maxHeight, decodeConfig, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                // TODO Auto-generated method stub
                Log.e(TAG, "ErrorStatus: " + arg0.toString());
            }
        });
    }
    public static void showPhoto_net_asynchronous(){
    }
    public static void showPhoto_native(int uri, ImageView imageView){
        imageView.setImageResource(uri);
    }
}

