package com.zhenye.myphtotloader.loadimageutils;

import android.widget.ImageView;

public abstract class AbsLoadImageUtils {
    public abstract void loadImage(ImageView imageView);//网络操作
    public abstract void loadUrl(String Url);
    public abstract void showImage(ImageView imageView, String Url);
}
