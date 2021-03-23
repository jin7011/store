package com.example.sns_project.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.sns_project.Info.PostInfo;
import com.example.sns_project.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    private Activity activity;
    private ArrayList<PostInfo> postList;

    public PostAdapter(Activity activity,ArrayList<PostInfo> postList) {
        this.postList = postList;
        this.activity = activity;
    }

    //holder
    static class PostHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함

        LinearLayout linearLayout;

        public PostHolder(@NonNull  LinearLayout linearLayout) {
            super(linearLayout);
            this.linearLayout = linearLayout;
        }
    }

    @NonNull
    @Override
    public PostAdapter.PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌

        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post,parent,false);
        final PostAdapter.PostHolder postHolder = new PostAdapter.PostHolder(linearLayout);

        return postHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostHolder holder, int position) { //포지션에 맞게 이미지 셋업

        String title = postList.get(position).getTitle();
        String content = postList.get(position).getContents();
        ArrayList<String> formats= postList.get(position).getFormats();
        Date date = postList.get(position).getCreatedAt();

        LinearLayout linearLayout = holder.linearLayout;

        TextView titleT = linearLayout.findViewById(R.id.titleT);
        TextView contentT = linearLayout.findViewById(R.id.contentT);
        TextView dateT = linearLayout.findViewById(R.id.dateT);
        ImageView imageView = linearLayout.findViewById(R.id.postImage);

        if(formats != null){
            imageView.setVisibility(View.VISIBLE);
            Glide.with(activity).load(formats.get(0)).transform(new CenterCrop(),new RoundedCorners(85)).override(800).thumbnail(0.5f).into(imageView);
        }
        titleT.setText(title);
        contentT.setText(content);
        dateT.setText(new SimpleDateFormat("MM/dd", Locale.getDefault()).format(date).toString());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    private void myStartActivity(Class c, PostInfo postInfo) {
        Intent intent = new Intent(activity, c);
        intent.putExtra("postInfo", postInfo);
        activity.startActivity(intent);
    }

}
