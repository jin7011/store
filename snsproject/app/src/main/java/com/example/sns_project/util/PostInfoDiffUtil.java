package com.example.sns_project.util;

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
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final PostInfo oldpost = oldPosts.get(oldItemPosition);
        final PostInfo newpost = newPosts.get(newItemPosition);

        return oldpost.getDocid().equals(newpost.getDocid());
    }

}
