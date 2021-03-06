package com.example.sns_project.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.example.sns_project.Adapter.Formats_PagerAdapter;
import com.example.sns_project.R;

import java.util.ArrayList;

public class View_FormatActivity extends AppCompatActivity {

    private ArrayList<String> formats;
    private int position;

    ViewPager2 viewPager2; //뷰페이저
    Formats_PagerAdapter viewPagerAdapter;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_formats_pager);

        Intent intent = getIntent();
        formats = (ArrayList<String>) intent.getSerializableExtra("formats");
        position = intent.getIntExtra("position",0);

        viewPager2 = findViewById(R.id.formats_pager);
        viewPagerAdapter = new Formats_PagerAdapter(this ,formats); //뷰페이저 어뎁터 생성
        viewPager2.setAdapter(viewPagerAdapter);//어뎁터 연결
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL); //스크롤방향
        viewPager2.setCurrentItem(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewPagerAdapter.exoUtil.just_releasePlayer(); //exo에게 자유를 주자
    }
}
