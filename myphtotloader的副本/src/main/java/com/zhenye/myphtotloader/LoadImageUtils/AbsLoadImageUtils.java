package com.zhenye.myphtotloader.loadimageutils;

import android.provider.ContactsContract;
import android.widget.ImageView;

public abstract class AbsLoadImageUtils {
    public abstract void LoadImage(ImageView imageView);//网络操作
    public abstract void LoadUrl(String Url);
    public abstract void ShowImage();
    public abstract void LruInit();
}
