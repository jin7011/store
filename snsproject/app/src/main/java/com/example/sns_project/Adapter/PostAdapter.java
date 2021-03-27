package com.example.sns_project.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.sns_project.Activities.PostActivity;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    private Activity activity;
    private ArrayList<PostInfo> postList;

    public PostAdapter(Activity activity,ArrayList<PostInfo> postList) {
        this.postList = postList;
        this.activity = activity;
    }

    //holder
    public class PostHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함

        TextView titleT ;
        TextView contentT ;
        TextView dateT ;
        ImageView imageView;

        public PostHolder(@NonNull View itemView) {
            super(itemView);

            titleT = itemView.findViewById(R.id.titleT);
            contentT = itemView.findViewById(R.id.contentT);
            dateT = itemView.findViewById(R.id.dateT);
            imageView = itemView.findViewById(R.id.postImage);

        }
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌

        View view  =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post,parent,false);
        final PostAdapter.PostHolder postHolder = new PostAdapter.PostHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, PostActivity.class);
                Log.d("포스트어댑터","uid: "+postList.get(postHolder.getAdapterPosition()).getId());
                intent.putExtra("postInfo", postList.get(postHolder.getAdapterPosition()));
                activity.startActivity(intent);
            }
        });

        return postHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostHolder holder, int position) { //포지션에 맞게 이미지 셋업

        PostInfo postInfo = postList.get(position);

        holder.titleT.setText(postInfo.getTitle());
        holder.contentT.setText(postInfo.getContents());
        holder.dateT.setText(new SimpleDateFormat("MM/dd", Locale.getDefault()).format(postInfo.getCreatedAt()));

        if(postInfo.getFormats() != null){

            String format = postInfo.getFormats().get(0);
            Log.d("P","foramt: "+format);
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(activity).load(format).transform(new CenterCrop(),new RoundedCorners(50)).thumbnail(0.5f).into(holder.imageView);

        }

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
