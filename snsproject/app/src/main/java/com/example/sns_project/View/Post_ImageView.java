package com.example.sns_project.View;

import android.app.Activity;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sns_project.R;

public class Post_ImageView {

    public Activity activity;
    public Uri uri;

    public Post_ImageView(Activity activity, Uri uri) {
        this.activity = activity;
        this.uri = uri;
    }

    public void makeImage(){

        //여기 건드려야함 3/20일 컨테이너 내부에 사진이 있는지 없는지 체크하면서 추가하고 visible기능 넣어야함.
        RequestOptions option_circle = new RequestOptions().circleCrop();

        LinearLayout parent = activity.findViewById(R.id.imageContainerLayout);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams
                (
                ViewGroup.LayoutParams.MATCH_PARENT
                ,ViewGroup.LayoutParams.WRAP_CONTENT
                );

        ImageView imageView = new ImageView(activity);
        imageView.setLayoutParams(layoutParams);
        Glide.with(activity).load(uri).override(500).apply(option_circle).into(imageView);
        parent.addView(imageView);

    }
}
