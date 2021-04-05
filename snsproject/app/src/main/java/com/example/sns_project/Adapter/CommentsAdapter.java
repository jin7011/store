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
import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.CommentInfo_DiffUtil;
import com.example.sns_project.util.Named;
import com.example.sns_project.util.PostInfo_DiffUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.SEC;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsHolder> {

        private Activity activity;
        private ArrayList<CommentInfo> comments = new ArrayList<>();
        private Named named = new Named();

        public void CommentInfo_DiffUtil(ArrayList<CommentInfo> newcomments) {
            final CommentInfo_DiffUtil diffCallback = new CommentInfo_DiffUtil(this.comments, newcomments);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            this.comments.clear();
            this.comments.addAll(newcomments);
            diffResult.dispatchUpdatesTo(this);
        }

        public CommentsAdapter(Activity activity) {
            this.activity = activity;
        }

        //holder
        public class CommentsHolder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함

            TextView contentT ;
            TextView dateT ;
            TextView goodNum;
            TextView nicknameT;

            public CommentsHolder(@NonNull View itemView) {
                super(itemView);

                nicknameT = itemView.findViewById(R.id.nickname_commentT);
                contentT = itemView.findViewById(R.id.comment_commentT);
                dateT = itemView.findViewById(R.id.date_commentT);
                goodNum = itemView.findViewById(R.id.goodNum_commentT);

            }
        }

        @NonNull
        @Override
        public CommentsAdapter.CommentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //비어있는 홀더에 비어있는 이미지뷰를 만들어줌

            View view  =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comments,parent,false);
            CommentsAdapter.CommentsHolder commentsHolder = new CommentsAdapter.CommentsHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            return commentsHolder;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull CommentsAdapter.CommentsHolder holder, int position) { //포지션에 맞게 이미지 셋업

            //todo   java.lang.ClassCastException: java.util.HashMap cannot be cast to com.example.sns_project.info.CommentInfo
            CommentInfo commentInfo = comments.get(position);

            holder.contentT.setText(commentInfo.getContents());
            holder.dateT.setText(formatTimeString(commentInfo.getCreatedAt(),new Date()));
            holder.goodNum.setText(commentInfo.getGood()+"");
            holder.nicknameT.setText(commentInfo.getPublisher());

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }


//        @Override
//    public long getItemId(int position) {
//        return postList.get(position).hashCode();
//    }

//    @Override
//    public int getItemViewType(int position) {
//        return position;
//    }


        public static String formatTimeString(Date postdate,Date nowDate){

            long ctime = nowDate.getTime();
            long regTime = postdate.getTime();

            long diffTime = (ctime - regTime) / 1000;
            String msg = null;

            if (diffTime < SEC) {
                msg = "방금 전";
            } else if ((diffTime /= SEC) < MIN) {
                msg = diffTime + "분 전";
            } else if ((diffTime /= MIN) < HOUR) {
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

