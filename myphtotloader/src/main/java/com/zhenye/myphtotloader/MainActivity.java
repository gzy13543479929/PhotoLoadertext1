package com.zhenye.myphtotloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.zhenye.myphtotloader.loadimageutils.LoadImageUtils;

public class MainActivity extends AppCompatActivity {

    ImageView imageView1, imageView2;
    Button button1;

    LoadImageUtils loadImageUtils;

    String ImageUrl = "http://e.hiphotos.baidu.com/image/pic/item/4e4a20a4462309f7e41f5cfe760e0cf3d6cad6ee.jpg";

    final int RENEW_IMAGEVIEW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = findViewById(R.id.MyPhoto_Main_Image1);
        imageView2 = findViewById(R.id.MyPhoto_Main_Image2);
        button1 = findViewById(R.id.MyPhoto_Main_Button1);

        loadImageUtils = new LoadImageUtils(this);
        loadImageUtils.showImage(imageView1, ImageUrl);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImageUtils.showImage(imageView2, ImageUrl);
            }
        });
    }

}
