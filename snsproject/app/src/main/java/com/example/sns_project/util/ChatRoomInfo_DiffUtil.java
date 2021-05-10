package com.example.sns_project.util;

import androidx.recyclerview.widget.DiffUtil;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.LetterInfo;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.SEC;

public class ChatRoomInfo_DiffUtil extends DiffUtil.Callback{

    private final ArrayList<ChatRoomInfo> oldrooms;
    private final ArrayList<ChatRoomInfo> newrooms;

    public ChatRoomInfo_DiffUtil(ArrayList<ChatRoomInfo> oldrooms, ArrayList<ChatRoomInfo> newrooms) {
        this.oldrooms = oldrooms;
        this.newrooms = newrooms;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        ChatRoomInfo oldroom = oldrooms.get(oldItemPosition);
        ChatRoomInfo newroom = newrooms.get(oldItemPosition);

        return oldroom.getCreatedAt().getTime() == newroom.getCreatedAt().getTime();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ChatRoomInfo oldroom = oldrooms.get(oldItemPosition);
        ChatRoomInfo newroom = newrooms.get(oldItemPosition);

        return oldroom.getKey().equals(newroom.getKey()) && IsSameLetter(oldroom.getLetters(),newroom.getLetters()) && oldroom.getReceiver_id().equals(newroom.getReceiver_id())
                && oldroom.getSender_id().equals(newroom.getSender_id());
    }

    private boolean IsSameLetter(ArrayList<LetterInfo> oldletters,ArrayList<LetterInfo> newletters){
        if(oldletters.size() == newletters.size()){
            for(int x=0; x<oldletters.size(); x++){
                LetterInfo oldletter = oldletters.get(x);
                LetterInfo newletter = newletters.get(x);

                return oldletter.getCreatedAt().getTime() != newletter.getCreatedAt().getTime() || !oldletter.getContents().equals(newletter.getContents())
                || !oldletter.getReciever_id().equals(newletter.getReciever_id()) || !oldletter.getSender_id().equals(newletter.getSender_id());
            }
        }else
            return false;

        return true;
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

    @Override
    public int getOldListSize() {
        return oldrooms.size();
    }
    @Override
    public int getNewListSize() {
        return newrooms.size();
    }


}
