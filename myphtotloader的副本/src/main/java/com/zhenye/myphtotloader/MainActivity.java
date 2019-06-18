package com.zhenye.myphtotloader;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.zhenye.myphtotloader.loadimageutils.LoadImageUtils;
public class MainActivity extends AppCompatActivity {

    ImageView imageView1,imageView2;
    Button button1 ;
    LoadImageUtils loadImageUtils = new LoadImageUtils();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView1 = findViewById(R.id.MyPhoto_Main_Image1);
        imageView2 = findViewById(R.id.MyPhoto_Main_Image2);
        button1 = findViewById(R.id.MyPhoto_Main_Button1);


        final String  ImageUrl = "http://g.hiphotos.baidu.com/image/pic/item/6d81800a19d8bc3e770bd00d868ba61ea9d345f2.jpg";
        loadImageUtils.LruInit();
        loadImageUtils.LoadUrl(ImageUrl);
        loadImageUtils.LoadImage(imageView1);
        loadImageUtils.ShowImage();


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            imageView2.setImageBitmap(loadImageUtils.ReturnBitmap(ImageUrl));
            }
        });
    }
}
