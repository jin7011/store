package com.example.sns_project.util;

import androidx.recyclerview.widget.DiffUtil;

import com.example.sns_project.info.LetterInfo;
import java.util.ArrayList;

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

        return oldletter.getCreatedAt().getTime() == newletter.getCreatedAt().getTime();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

        LetterInfo oldletter = oldletters.get(oldItemPosition);
        LetterInfo newletter = newletters.get(newItemPosition);

        return oldletter.getSender_id().equals(newletter.getSender_id())
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

}
