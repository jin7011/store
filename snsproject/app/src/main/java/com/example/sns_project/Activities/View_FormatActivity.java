package com.example.sns_project.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.ayokunlepaul.frescoloader.FrescoMediaHelper;
import com.bumptech.glide.Glide;
import com.example.sns_project.Adapter.Formats_PagerAdapter;
import com.example.sns_project.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.loader.picasso.PicassoImageLoader;
import com.veinhorn.scrollgalleryview.loader.picasso.PicassoMediaHelper;

import java.net.URLConnection;
import java.util.ArrayList;

import ogbe.ozioma.com.glideimageloader.GlideImageLoader;
import ogbe.ozioma.com.glideimageloader.GlideMediaHelper;

import static com.veinhorn.scrollgalleryview.loader.picasso.dsl.DSL.video;

public class View_FormatActivity extends AppCompatActivity {

    private ArrayList<String> formats;
    private int position;

    ViewPager2 viewPager2; //뷰페이저
    Formats_PagerAdapter viewPagerAdapter;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_formatactivity);

        Intent intent = getIntent();
        formats = (ArrayList<String>) intent.getSerializableExtra("formats");
        position = intent.getIntExtra("position",0);

        viewPager2 = findViewById(R.id.formats_pager);
        viewPagerAdapter = new Formats_PagerAdapter(this ,formats); //뷰페이저 어뎁터 생성
        viewPager2.setAdapter(viewPagerAdapter);//어뎁터 연결
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL); //스크롤방향
        viewPager2.setCurrentItem(position);

    }

}
