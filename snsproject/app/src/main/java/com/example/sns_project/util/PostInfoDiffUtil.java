package com.example.sns_project.util;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sns_project.info.PostInfo;

import java.util.ArrayList;

public class PostInfoDiffUtil extends DiffUtil.Callback{

    private final ArrayList<PostInfo> oldPosts;
    private final ArrayList<PostInfo> newPosts;

    public PostInfoDiffUtil(ArrayList<PostInfo> oldPosts, ArrayList<PostInfo> newPosts) {
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

        return oldpost.getDocid().equals(newpost.getDocid()) && oldpost.getGood() == newpost.getGood() && oldpost.getComment() == newpost.getComment();
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition,int newItemPosition) {
        //Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }


}
