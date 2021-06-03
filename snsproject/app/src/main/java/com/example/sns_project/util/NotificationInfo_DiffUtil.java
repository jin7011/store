package com.example.sns_project.util;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.sns_project.info.NotificationInfo;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.CustomLibrary.PostControler.MessageTime_to_String;

public class NotificationInfo_DiffUtil extends DiffUtil.Callback {

    private final ArrayList<NotificationInfo> oldNotis;
    private final ArrayList<NotificationInfo> newNotis;

    public NotificationInfo_DiffUtil(ArrayList<NotificationInfo> oldNotis, ArrayList<NotificationInfo> newNotis) {
        this.oldNotis = oldNotis;
        this.newNotis = newNotis;
    }

    @Override
    public int getOldListSize() {
        return oldNotis.size();
    }

    @Override
    public int getNewListSize() {
        return newNotis.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        final NotificationInfo oldnoti = oldNotis.get(oldItemPosition);
        final NotificationInfo newnoti = newNotis.get(newItemPosition);

        return oldnoti.getDocid().equals(newnoti.getDocid());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) { //item이 같아도 수정된다면 내용이 다르다는 것을 인식시켜줘야 내용이 바뀜
        final NotificationInfo oldnoti = oldNotis.get(oldItemPosition);
        final NotificationInfo newnoti = newNotis.get(newItemPosition);

        return  MessageTime_to_String(oldnoti.getCreatedAt(),new Date()).equals(MessageTime_to_String(newnoti.getCreatedAt(),new Date()))
                && oldnoti.getContents().equals(newnoti.getContents())
                && oldnoti.getTopic().equals(newnoti.getTopic())
                ;

    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition,int newItemPosition) {
        //Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}