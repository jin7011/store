package com.example.sns_project.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.R;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.NotificationInfo;

import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.CustomLibrary.PostControler.MessageTime_to_String;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<NotificationInfo> notis;

    public NotificationAdapter(ArrayList<NotificationInfo> notis){
        this.notis = notis;
    }

    //holder
    public static class Noti_holder extends RecyclerView.ViewHolder {
        TextView sender_nick ;
        TextView content ;
        TextView date;

        public Noti_holder (@NonNull View itemView) {
            super(itemView);
            sender_nick = itemView.findViewById(R.id.Notification_LocationT);
            content = itemView.findViewById(R.id.Notification_content);
            date = itemView.findViewById(R.id.Notification_date);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        Noti_holder noti_holder = new Noti_holder(view);

        return noti_holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Noti_holder noti_holder  = (Noti_holder)holder;
        NotificationInfo noti = notis.get(position);

        noti_holder.sender_nick.setText(noti.getSender()+" "+noti.getType());
        noti_holder.content.setText(noti.getContents());
        noti_holder.date.setText(MessageTime_to_String(noti.getCreatedAt(),new Date()));
    }

    @Override
    public int getItemCount() {
        return notis.size();
    }
}
