package com.example.sns_project.Adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.R;
import com.example.sns_project.info.NotificationInfo;
import com.example.sns_project.util.NotificationInfo_DiffUtil;

import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.CustomLibrary.PostControler.MessageTime_to_String;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<NotificationInfo> notis = new ArrayList<>();

    public void NotificationInfo_DiffUtil(ArrayList<NotificationInfo> Newnotis) {
        final NotificationInfo_DiffUtil diffCallback = new NotificationInfo_DiffUtil(this.notis,Newnotis);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.notis.clear();
        this.notis.addAll(Newnotis);

        diffResult.dispatchUpdatesTo(this);
    }

    public NotificationAdapter(){ }

    //holder
    public static class Noti_holder extends RecyclerView.ViewHolder {
        TextView sender_nick ;
        TextView content ;
        TextView date;
        ImageView image;

        public Noti_holder (@NonNull View itemView) {
            super(itemView);
            sender_nick = itemView.findViewById(R.id.Notification_LocationT);
            content = itemView.findViewById(R.id.Notification_content);
            date = itemView.findViewById(R.id.Notification_date);
            image = itemView.findViewById(R.id.noti_image);
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

        if(noti.getTopic().equals("Message")) {
            noti_holder.sender_nick.setText(noti.getSender() + " " + "님이 쪽지를 보냈습니다.");
            noti_holder.image.setBackgroundResource(R.drawable.noti_message_bubble);
            noti_holder.image.setImageResource(R.drawable.noti_foot);
        }
        if(noti.getTopic().equals("Comment"))
            noti_holder.sender_nick.setText(noti.getSender()+" "+"게시물에 댓글이 달렸습니다.");
        if(noti.getTopic().equals("Recomment"))
            noti_holder.sender_nick.setText(noti.getSender()+" "+"님이 대댓글을 남겼습니다.");

        Log.d("zxczxc123123",noti.getTopic());
        Log.d("zxczxc123123",noti.getTopic().equals("Message")+"");
        noti_holder.content.setText(noti.getContents());
        noti_holder.date.setText(MessageTime_to_String(noti.getCreatedAt(),new Date()));
    }

    @Override
    public int getItemCount() {
        return notis.size();
    }
}
