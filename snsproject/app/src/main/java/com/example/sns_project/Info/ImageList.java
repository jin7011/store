package com.example.sns_project.Info;

import android.net.Uri;

import java.util.ArrayList;

public class ImageList { //싱글톤으로 객체를 내부에서 생성함으로써, 내부에 있는 List를 다 같이 돌려써보자(사진넣는 글쓰기, 리사이클러 어댑터에서 공용으로 사용중)

    public static final ArrayList<Uri> ImageList = new ArrayList<>();

    public ImageList() {}

    private static class LazyHolder {
        public static final ImageList imageList = new ImageList();
    }

    public static ImageList getimageList() {
        return LazyHolder.imageList;
    }

    public  void add(Uri uri){
        ImageList.add(uri);
    }

    public  ArrayList<Uri> getImageList() {
        return ImageList;
    }
}
