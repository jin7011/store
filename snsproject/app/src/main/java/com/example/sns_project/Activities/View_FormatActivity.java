package com.example.sns_project.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.ayokunlepaul.frescoloader.FrescoMediaHelper;
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

    protected ScrollGalleryView scrollGalleryView;
    protected SpinKitView progressBar;
    protected FrameLayout loading;
    private ArrayList<String> formats;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_formatactivity);

        scrollGalleryView = findViewById(R.id.scroll_gallery_view);
        progressBar = findViewById(R.id.spin_kit);
        loading = findViewById(R.id.loading);

        Intent intent = getIntent();
        formats = (ArrayList<String>) intent.getSerializableExtra("formats");
        position = intent.getIntExtra("position",0);

        scrollGalleryView
                .setThumbnailSize(300)
                .setZoom(true)
                .withHiddenThumbnails(true)
                .hideThumbnailsOnClick(true)
                .hideThumbnailsAfter(5000)
                .setFragmentManager(getSupportFragmentManager());

        loading.setVisibility(View.VISIBLE);
        for (String imageUrl : formats) {
            setUrl(imageUrl);
            Log.d("klklklklk",imageUrl);
        }
        loading.setVisibility(View.INVISIBLE);
    }

    public void setUrl(String url) {
        if (isVideo(url)) { //비디오라면
            scrollGalleryView.addMedia(video(url,R.drawable.play));
            Log.d("clclclclclc","video");
        } else { //사진이라면
            scrollGalleryView.addMedia(new PicassoMediaHelper().image(url));
            Log.d("clclclclclc","image");
        }
    }

    //확장자가 비디오인지 이미지인지 확인
    private Boolean isVideo(String path) {
        String extension = getExtension(path);

        //todo gif도 처리해보자. 현재로써는 gif파일을 전체화면으로는 다루지않음. 썸네일로는 가능(glide).
//        if (extension.contains("mp4") || extension.contains("MP4") || extension.contains("MOV") || extension.contains("mov") || extension.contains("AVI") || extension.contains("avi") ||
//                extension.contains("MKV") || extension.contains("mkv") || extension.contains("WMV") || extension.contains("wmv") || extension.contains("TS") || extension.contains("ts") ||
//                extension.contains("TP") || extension.contains("tp") || extension.contains("FLV") || extension.contains("flv") || extension.contains("3GP") || extension.contains("3gp") ||
//                extension.contains("MPG") || extension.contains("mpg") || extension.contains("MPEG") || extension.contains("mpeg") || extension.contains("MPE") || extension.contains("mpe") ||
//                extension.contains("ASF") || extension.contains("asf") || extension.contains("ASX") || extension.contains("asx") || extension.contains("DAT") || extension.contains("dat") ||
//                extension.contains("RM") || extension.contains("rm"))
        if (extension.contains("mp4") || extension.contains("MP4") || extension.contains("MOV") || extension.contains("mov") || extension.contains("AVI") || extension.contains("avi") ||
                extension.contains("MKV") || extension.contains("mkv") || extension.contains("WMV") || extension.contains("wmv") || extension.contains("TS") || extension.contains("ts") ||
                extension.contains("TP") || extension.contains("tp") || extension.contains("FLV") || extension.contains("flv") || extension.contains("3GP") || extension.contains("3gp") ||
                extension.contains("MPG") || extension.contains("mpg") || extension.contains("MPEG") || extension.contains("mpeg") || extension.contains("MPE") || extension.contains("mpe") ||
                extension.contains("ASF") || extension.contains("asf") || extension.contains("ASX") || extension.contains("asx") || extension.contains("DAT") || extension.contains("dat") ||
                extension.contains("RM") || extension.contains("rm"))
        {
            return true;
        } else {
            return false;
        }
    }

//    public static boolean isImageFile(String path) {
//        String mimeType = URLConnection.guessContentTypeFromName(path);
//        return mimeType != null && mimeType.startsWith("image");
//    }
//
//    public static boolean isVideoFile(String path) {
//        String mimeType = URLConnection.guessContentTypeFromName(path);
//        return mimeType != null && mimeType.startsWith("video");
//    }

    //확장자 나누기
    private String getExtension(String url) {
        return url.substring(url.lastIndexOf(".") + 1, url.lastIndexOf("?")+1);
    }

}
