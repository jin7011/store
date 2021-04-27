package com.example.sns_project.util;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sns_project.info.PostInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.SEC;

public class PostInfo_DiffUtil extends DiffUtil.Callback{

    private final ArrayList<PostInfo> oldPosts;
    private final ArrayList<PostInfo> newPosts;

    public PostInfo_DiffUtil(ArrayList<PostInfo> oldPosts, ArrayList<PostInfo> newPosts) {
        this.oldPosts = oldPosts;
        this.newPosts = newPosts;
    }

    @Override
    public int getOldListSize() {
        return oldPosts.size();
    }

    @Override
    public int getNewListSize() {
        return newPosts.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        final PostInfo oldpost = oldPosts.get(oldItemPosition);
        final PostInfo newpost = newPosts.get(newItemPosition);

        return oldpost.getDocid().equals(newpost.getDocid());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) { //item이 같아도 수정된다면 내용이 다르다는 것을 인식시켜줘야 내용이 바뀜
        final PostInfo oldpost = oldPosts.get(oldItemPosition);
        final PostInfo newpost = newPosts.get(newItemPosition);
        Log.d("같나","old: "+ oldpost.getGood()+" oldid: "+oldpost.getDocid()+" new: "+ newpost.getGood()+" newid: "+newpost.getDocid());

        if(oldpost.getHow_Long() != null && newpost.getHow_Long() == null) { //새로 받아온 배열이 기존의 시간과 차이가 있을 때 표기(방금전,3분전)를 다르게 하기 위해서
            Date date = new Date();
            newpost.setHow_Long(formatTimeString(newpost.getCreatedAt(), date));

            return oldpost.getDocid().equals(newpost.getDocid()) && oldpost.getGood() == newpost.getGood() && oldpost.getComment() == newpost.getComment()
                    &&oldpost.getHow_Long().equals(newpost.getHow_Long());
        }
        else
            return oldpost.getDocid().equals(newpost.getDocid()) && oldpost.getGood() == newpost.getGood() && oldpost.getComment() == newpost.getComment();
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition,int newItemPosition) {
        //Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

    public static String formatTimeString(Date postdate, Date nowDate){

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
