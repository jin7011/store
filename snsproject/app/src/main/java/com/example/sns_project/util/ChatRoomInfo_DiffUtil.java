package com.example.sns_project.util;

import androidx.recyclerview.widget.DiffUtil;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.LetterInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.CustomLibrary.PostControler.MessageTime_to_String;
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

        return oldroom.getKey().equals(newroom.getKey());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ChatRoomInfo oldroom = oldrooms.get(oldItemPosition);
        ChatRoomInfo newroom = newrooms.get(oldItemPosition);



        return oldroom.getKey().equals(newroom.getKey()) && oldroom.getUser2_id().equals(newroom.getUser2_id())
                && oldroom.getUser1_id().equals(newroom.getUser1_id()) && oldroom.getUser1_OutDate().equals(newroom.getUser1_OutDate()) &&
                oldroom.getUser2_OutDate().equals(newroom.getUser2_OutDate())
                && MessageTime_to_String(oldroom.getLatestDate(),new Date()).equals(MessageTime_to_String(newroom.getLatestDate(),new Date()))
                && oldroom.getUser1_count() == newroom.getUser1_count()
                && oldroom.getUser2_count() == newroom.getUser2_count()
                && oldroom.getLatestMessage().equals(newroom.getLatestMessage());
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
