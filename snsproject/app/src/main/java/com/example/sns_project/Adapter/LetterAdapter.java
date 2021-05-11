package com.example.sns_project.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sns_project.R;
import com.example.sns_project.info.LetterInfo;
import com.example.sns_project.util.LetterInfo_DiffUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.MINE;
import static com.example.sns_project.util.Named.OTHER;
import static com.example.sns_project.util.Named.SEC;

public class LetterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<LetterInfo> letters;
    private Activity activity;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public LetterAdapter(Activity activity){
        this.activity = activity;
        this.letters = new ArrayList<>();
    }

    public void LetterInfoDiffUtil(ArrayList<LetterInfo> NewLetters) {
        final LetterInfo_DiffUtil diffCallback = new LetterInfo_DiffUtil(this.letters, NewLetters);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        letters.clear();
        letters.addAll(NewLetters);

        diffResult.dispatchUpdatesTo(this);
    }

    //holder
    public static class Other_Holder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        //R.layout.item_comments에 존재하지 않는 뷰는 일반적으로는 설정하나마나임
        TextView other_msg ;
        TextView other_time ;
        TextView other_nick;

        public Other_Holder (@NonNull View itemView) {
            super(itemView);
            other_time = itemView.findViewById(R.id.other_time);
            other_msg = itemView.findViewById(R.id.other_msg);
            other_nick = itemView.findViewById(R.id.other_nick);
        }
    }

    //holder
    public static class My_Holder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        //R.layout.item_comments에 존재하지 않는 뷰는 일반적으로는 설정하나마나임
        TextView my_msg ;
        TextView my_time ;
        TextView my_nick;

        public My_Holder (@NonNull View itemView) {
            super(itemView);
            my_time = itemView.findViewById(R.id.my_time);
            my_msg = itemView.findViewById(R.id.my_msg);
            my_nick = itemView.findViewById(R.id.my_nick);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MINE){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_letter_others, parent, false);
            LetterAdapter.Other_Holder other = new LetterAdapter.Other_Holder(view);
            return other;
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_letter_mine, parent, false);
            LetterAdapter.My_Holder mine = new LetterAdapter.My_Holder(view);
            return mine;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        LetterInfo letter = letters.get(position);

        if(holder instanceof Other_Holder){
            Other_Holder otherHolder = (Other_Holder)holder;
            otherHolder.other_nick.setText(letter.getSender_nick());
            otherHolder.other_msg.setText(letter.getContents());
            otherHolder.other_time.setText(formatTimeString(letter.getCreatedAt(),new Date()));
        }

        if(holder instanceof My_Holder){
            My_Holder myHolder = (My_Holder)holder;
            myHolder.my_nick.setText(letter.getSender_nick());
            myHolder.my_msg.setText(letter.getContents());
            myHolder.my_time.setText(formatTimeString(letter.getCreatedAt(),new Date()));
        }

    }

    @Override
    public int getItemCount() {
        return letters.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(letters.get(position).getReciever_id().equals(user.getUid()))
            return MINE;
        else
            return OTHER;
    }

    public static String formatTimeString(Date postdate, Date nowDate){

        long ctime = nowDate.getTime();
        long regTime = postdate.getTime();

        long diffTime = (ctime - regTime) / 1000;
        String msg;

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
