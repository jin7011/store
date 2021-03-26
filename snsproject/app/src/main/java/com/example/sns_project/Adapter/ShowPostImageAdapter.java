package com.example.sns_project.Adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.sns_project.R;

import java.util.ArrayList;

public class ShowPostImageAdapter extends RecyclerView.Adapter<ShowPostImageAdapter.ShowPostImageHolder>{

    private Activity activity;
    private ArrayList<String> formats;

    public ShowPostImageAdapter(Activity activity, ArrayList<String> formats) {
        this.activity = activity;
        this.formats = formats;
    }

    //holder
    static class ShowPostImageHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        ImageView imageView;

        public ShowPostImageHolder(@NonNull ImageView imageView) {
            super(imageView);
            this.imageView = imageView;
        }
    }

    @NonNull
    @Override
    public ShowPostImageAdapter.ShowPostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌

        ImageView imageView = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_addimage,parent,false);
        ShowPostImageHolder showPostImageHolder = new ShowPostImageHolder(imageView);
        Log.d("holder","holder: "+showPostImageHolder.getAdapterPosition());

        return showPostImageHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ShowPostImageHolder holder, int position) { //포지션에 맞게 이미지 셋업

        ImageView imageView = holder.imageView;
        Glide.with(activity).load(formats.get(position)).transform(new CenterCrop(), new RoundedCorners(85)).override(600,700).into(imageView);
        Log.d("포멧리사이클러뷰","성공: "+position+ "url: " +formats.get(position));


//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return formats.size();
    }


}
