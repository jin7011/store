package com.example.sns_project.Adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.R;

public class LetterAdapter {







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
        TextView other_msg ;
        TextView other_time ;
        TextView other_nick;

        public My_Holder (@NonNull View itemView) {
            super(itemView);
            other_time = itemView.findViewById(R.id.my_time);
            other_msg = itemView.findViewById(R.id.my_msg);
            other_nick = itemView.findViewById(R.id.my_nick);
        }
    }
}
