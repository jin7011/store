package com.example.sns_project.util;

import android.util.Log;

import androidx.recyclerview.widget.DiffUtil;

import com.example.sns_project.info.LetterInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.SEC;

public class LetterInfo_DiffUtil extends DiffUtil.Callback{

    private final ArrayList<LetterInfo> oldletters;
    private final ArrayList<LetterInfo> newletters;

    public LetterInfo_DiffUtil(ArrayList<LetterInfo> oldletters, ArrayList<LetterInfo> newletters) {
        this.oldletters = oldletters;
        this.newletters = newletters;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {

        LetterInfo oldletter = oldletters.get(oldItemPosition);
        LetterInfo newletter = newletters.get(newItemPosition);

        return oldletter.getCreatedAt().equals(newletter.getCreatedAt());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

        LetterInfo oldletter = oldletters.get(oldItemPosition);
        LetterInfo newletter = newletters.get(newItemPosition);
        String olddate = formatTimeString(oldletter.getCreatedAt(),new Date());
        String newdate = formatTimeString(newletter.getCreatedAt(),new Date());

//        Log.d(
//                "elvm","old: " + olddate+" new: "+ newdate+" nowdate: "+new Date().getTime()
//               + " zxc: "+formatTimeString(oldletter.getCreatedAt(),new Date())
//                +" oldCreatedat: "+oldletter.getCreatedAt() + " new: "+newletter.getCreatedAt()
//                +" contetnt : "+oldletter.getContents()
//        );

        return oldletter.getSender_id().equals(newletter.getSender_id())
                && olddate.equals(newdate)
                && oldletter.getReciever_id().equals(newletter.getReciever_id())
                && oldletter.getContents().equals(newletter.getContents());
    }

    @Override
    public int getOldListSize() {
        return oldletters.size();
    }

    @Override
    public int getNewListSize() {
        return newletters.size();
    }


    public static String formatTimeString(long regTime, Date nowDate){

        long ctime = nowDate.getTime();
//        long regTime = postdate.getTime();

        long diffTime = Math.abs(ctime - regTime) / 1000;
//        Log.d("vhaptxkdl","createat: "+regTime + " cal: "+diffTime);
        String msg;

        if (diffTime < SEC) {
            msg = "방금 전";
        } else if ((diffTime /= SEC) < MIN) {
            msg = diffTime + "분 전";
//        } else if ((diffTime /= MIN) < HOUR) {
//            msg = new SimpleDateFormat("HH:mm").format(postdate);
//        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
//            msg = (diffTime) + "일 전";
//        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
//            msg = (diffTime) + "달 전";
        } else {
            msg = new SimpleDateFormat("HH:mm").format(new Date(regTime));
        }
        return msg;
    }

}
