package com.example.sns_project.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.sns_project.Activities.PostActivity;
import com.example.sns_project.R;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.PostInfoDiffUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    private Activity activity;
    private ArrayList<PostInfo> postList = new ArrayList<>();

    public void PostInfoDiffUtil(ArrayList<PostInfo> newPosts) {
        final PostInfoDiffUtil diffCallback = new PostInfoDiffUtil(this.postList, newPosts);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.postList.clear();
        this.postList.addAll(newPosts);
        diffResult.dispatchUpdatesTo(this);
    }

    public PostAdapter(Activity activity) {
        this.activity = activity;
    }

    //holder
    public class PostHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함

        TextView titleT ;
        TextView contentT ;
        TextView dateT ;
        TextView goodNum;
        TextView commentNum;
        ImageView imageView;
        TextView nicknameT;

        public PostHolder(@NonNull View itemView) {
            super(itemView);

            nicknameT = itemView.findViewById(R.id.nicknameT);
            titleT = itemView.findViewById(R.id.titleT);
            contentT = itemView.findViewById(R.id.contentT);
            dateT = itemView.findViewById(R.id.dateT);
            goodNum = itemView.findViewById(R.id.goodNum_postItem);
            commentNum = itemView.findViewById(R.id.commentNum_postItem);
            imageView = itemView.findViewById(R.id.postImage);

        }
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌

        View view  =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post,parent,false);
        PostAdapter.PostHolder postHolder = new PostAdapter.PostHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, PostActivity.class);
                Log.d("포스트어댑터","uid: "+postList.get(postHolder.getAdapterPosition()).getId());
                intent.putExtra("postInfo", postList.get(postHolder.getAdapterPosition()));
                activity.startActivityForResult(intent,2);
            }
        });

        return postHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostHolder holder, int position) { //포지션에 맞게 이미지 셋업

        PostInfo postInfo = postList.get(position);

        holder.titleT.setText(postInfo.getTitle());
        holder.contentT.setText(postInfo.getContents());
//        holder.dateT.setText(new SimpleDateFormat("MM/dd", Locale.getDefault()).format(postInfo.getCreatedAt()));
        holder.dateT.setText(formatTimeString(postInfo.getCreatedAt(),new Date()));
        holder.goodNum.setText(postInfo.getGood()+"");
        holder.commentNum.setText(postInfo.getComment()+"");
        holder.nicknameT.setText(postInfo.getPublisher());

        if(postInfo.getFormats() != null){

            String format = postInfo.getFormats().get(0);
            Log.d("bind 사진 바인드","foramt: "+format);
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(activity).load(format).transform(new CenterCrop(),new RoundedCorners(50)).thumbnail(0.5f).into(holder.imageView);

        }else{
            holder.imageView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


        @Override
    public long getItemId(int position) {
        return postList.get(position).hashCode();
    }

//    @Override
//    public int getItemViewType(int position) {
//        return position;
//    }

    private static class TIME_MAXIMUM{
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    public static String formatTimeString(Date postdate,Date nowDate){

        long ctime = nowDate.getTime();
        long regTime = postdate.getTime();

        long diffTime = (ctime - regTime) / 1000;
        String msg = null;

        if (diffTime < TIME_MAXIMUM.SEC) {
            msg = "방금 전";
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            msg = new SimpleDateFormat("HH:mm").format(postdate);
//        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
//            msg = (diffTime) + "일 전";
//        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
//            msg = (diffTime) + "달 전";
        } else {
            msg = new SimpleDateFormat("MM월dd일").format(postdate);
        }
        return msg;
    }

}
