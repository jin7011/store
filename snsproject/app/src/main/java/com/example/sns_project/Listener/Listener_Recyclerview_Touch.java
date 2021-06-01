package com.example.sns_project.Listener;

import androidx.recyclerview.widget.RecyclerView;

public interface Listener_Recyclerview_Touch {
//    boolean onItemMove(int from_position, int to_position);
    void onItemSwipe(RecyclerView.ViewHolder viewHolder);
//    void onLeftClick(int position, RecyclerView.ViewHolder viewHolder);
//    void onRightClick(int position, RecyclerView.ViewHolder viewHolder);
}
