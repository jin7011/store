package com.example.sns_project.util;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import com.example.sns_project.info.RecommentInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class RecommentInfo_DiffUtil extends DiffUtil.Callback {

    private final ArrayList<RecommentInfo> oldPosts;
    private final ArrayList<RecommentInfo> newPosts;

    public RecommentInfo_DiffUtil(ArrayList<RecommentInfo> oldPosts, ArrayList<RecommentInfo> newPosts) {
        this.oldPosts = oldPosts;
        this.newPosts = newPosts;

        Log.d("rfrf",newPosts.hashCode()+" , "+oldPosts.hashCode());
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
        final RecommentInfo oldpost = oldPosts.get(oldItemPosition);
        final RecommentInfo newpost = newPosts.get(newItemPosition);


        long olddate = oldpost.getCreatedAt().getTime();
        long newdate = newpost.getCreatedAt().getTime();

        return olddate == newdate;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) { //item이 같아도 수정된다면 내용이 다르다는 것을 인식시켜줘야 내용이 바뀜
        final RecommentInfo oldpost = oldPosts.get(oldItemPosition);
        final RecommentInfo newpost = newPosts.get(newItemPosition);

        return oldpost.getContents().equals(newpost.getContents()) && oldpost.getId().equals(newpost.getId()) && oldpost.getGood() == newpost.getGood();
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
