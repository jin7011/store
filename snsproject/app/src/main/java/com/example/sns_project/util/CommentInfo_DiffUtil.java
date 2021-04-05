package com.example.sns_project.util;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.PostInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CommentInfo_DiffUtil extends DiffUtil.Callback {

    private final ArrayList<CommentInfo> oldPosts;
    private final ArrayList<CommentInfo> newPosts;

    public CommentInfo_DiffUtil(ArrayList<CommentInfo> oldPosts, ArrayList<CommentInfo> newPosts) {
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
        final CommentInfo oldpost = oldPosts.get(oldItemPosition);
        final CommentInfo newpost = newPosts.get(newItemPosition);

        // todo  java.lang.IllegalArgumentException: Cannot format given Object as a Date
        String olddate = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss").format(oldpost.getCreatedAt());
        String newDate = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss").format(newpost.getCreatedAt());

        return olddate.equals(newDate);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) { //item이 같아도 수정된다면 내용이 다르다는 것을 인식시켜줘야 내용이 바뀜
        final CommentInfo oldpost = oldPosts.get(oldItemPosition);
        final CommentInfo newpost = newPosts.get(newItemPosition);

        return oldpost.getContents().equals(newpost.getContents()) && oldpost.getId().equals(newpost.getId());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
