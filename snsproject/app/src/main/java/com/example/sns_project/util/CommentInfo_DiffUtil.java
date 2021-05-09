package com.example.sns_project.util;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.info.RecommentInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.SEC;

public class CommentInfo_DiffUtil extends DiffUtil.Callback {

    private final ArrayList<CommentInfo> OldComments;
    private final ArrayList<CommentInfo> NewComments;
    private final PostInfo OldPost;
    private final PostInfo NewPost;

    public CommentInfo_DiffUtil(PostInfo OldPost, PostInfo NewPost) {
        this.OldPost = OldPost;
        this.NewPost = NewPost;
        this.OldComments = this.OldPost.getComments();
        this.NewComments = this.NewPost.getComments();
        Log.d("같z나zxc","old: " + OldPost.hashCode() + " new: "+ NewPost.hashCode());
    }

    @Override
    public int getOldListSize() {
        return OldComments.size();
    }

    @Override
    public int getNewListSize() {
        return NewComments.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        final CommentInfo oldpost = OldComments.get(oldItemPosition);
        final CommentInfo newpost = NewComments.get(newItemPosition);

        return oldpost.getKey().equals(newpost.getKey());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) { //item이 같아도 수정된다면 내용이 다르다는 것을 인식시켜줘야 내용이 바뀜
        final CommentInfo oldpost = OldComments.get(oldItemPosition);
        final CommentInfo newpost = NewComments.get(newItemPosition);

                Log.d("같나","old: "+is_same_recomment(oldpost,newpost));

        return oldpost.getContents().equals(newpost.getContents()) && oldpost.getId().equals(newpost.getId()) && is_same_recomment(oldpost,newpost) && oldpost.getGood() == newpost.getGood();
    }

    private boolean is_same_recomment(CommentInfo oldpost, CommentInfo newpost) {
        ArrayList<RecommentInfo> oldrecomments = oldpost.getRecomments();
        ArrayList<RecommentInfo> newrecomments = newpost.getRecomments();

//        Log.d("같나","old: " + oldrecomments.hashCode() + " new: "+ newrecomments.hashCode());

        if(oldrecomments.size() == newrecomments.size()){
            for(int x=0; x<newrecomments.size(); x++){
                long olddate = oldrecomments.get(x).getCreatedAt().getTime();
                long newdate = newrecomments.get(x).getCreatedAt().getTime();
                int oldgood =  oldrecomments.get(x).getGood();
                int newgood = newrecomments.get(x).getGood();

                if(olddate != newdate || oldgood != newgood)
                    return false;
            }
            return true;
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
