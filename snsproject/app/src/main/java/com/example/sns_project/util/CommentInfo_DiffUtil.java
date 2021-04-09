package com.example.sns_project.util;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.info.RecommentInfo;

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

        return oldpost.getKey().equals(newpost.getKey());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) { //item이 같아도 수정된다면 내용이 다르다는 것을 인식시켜줘야 내용이 바뀜
        final CommentInfo oldpost = oldPosts.get(oldItemPosition);
        final CommentInfo newpost = newPosts.get(newItemPosition);

        return oldpost.getContents().equals(newpost.getContents()) && oldpost.getId().equals(newpost.getId()) && is_same_recomment(oldpost,newpost);
    }

    private boolean is_same_recomment(CommentInfo oldpost, CommentInfo newpost) {
        ArrayList<RecommentInfo> oldrecomments = oldpost.getRecomments();
        ArrayList<RecommentInfo> newrecomments = newpost.getRecomments();

        if(oldrecomments.size() == newrecomments.size()){
            for(int x=0; x<newrecomments.size(); x++){
                long olddate = oldrecomments.get(x).getCreatedAt().getTime();
                long newdate = newrecomments.get(x).getCreatedAt().getTime();

                if(olddate == newdate)
                    return true;
            }
            return false;
        }else{
            return false;
        }
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        //Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
