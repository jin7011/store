package com.example.sns_project.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.activity.ChatRoomActivity;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.util.ChatRoomInfo_DiffUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;


public class ChatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final Activity activity;
    private final PostControler postControler;
    private final ArrayList<ChatRoomInfo> ChatRooms;

    public void ChatRoomInfo_DiffUtile(ArrayList<ChatRoomInfo> NewChatRooms) {
        final ChatRoomInfo_DiffUtil diffCallback = new ChatRoomInfo_DiffUtil(this.ChatRooms, NewChatRooms);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.ChatRooms.clear();
        this.ChatRooms.addAll(NewChatRooms);

        diffResult.dispatchUpdatesTo(this);
    }

    public ChatRoomAdapter(Activity activity, PostControler postControler) {
        this.activity = activity;
        this.ChatRooms = new ArrayList<>();
        this.postControler = postControler;
    }

    //holder
    public static class Room_Holder extends RecyclerView.ViewHolder {
        TextView sender_nick ;
        TextView Current_time ;
        TextView Current_msg;
        TextView Letter_Count;

        public Room_Holder (@NonNull View itemView) {
            super(itemView);
            sender_nick = itemView.findViewById(R.id.Room_nicknameT);
            Current_time = itemView.findViewById(R.id.Room_dateT);
            Current_msg = itemView.findViewById(R.id.Room_contentT);
            Letter_Count = itemView.findViewById(R.id.Letter_countT);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        Room_Holder roomHolder = new Room_Holder(view);

        return roomHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Room_Holder roomHolder = (Room_Holder)holder;
        ChatRoomInfo room = ChatRooms.get(position);
        String sender = room.getUser1_id().equals(user.getUid()) ? room.getUser2() : room.getUser1();
        int count = room.getUser1_id().equals(user.getUid()) ? room.getUser1_count() : room.getUser2_count();

        ((Room_Holder) holder).itemView.setOnClickListener(new View.OnClickListener() { //todo 방만드는 거는 완료 했다고 보는데 계속 실시간데이터 연결이 끊겨서 더이상 못써먹겠음 채팅내용도 전부 store로 옮기자.
            @Override
            public void onClick(View v) {
                ChatRoomInfo room = ChatRooms.get(position);
                String user_nick = room.getUser1_id().equals(user.getUid()) ? room.getUser2() : room.getUser1();
                String user_id =  room.getUser1_id().equals(user.getUid()) ? room.getUser2_id() : room.getUser1_id();
                Intent intent = new Intent(activity, ChatRoomActivity.class);
                Log.d("dnwjdkel","user_nick: " + user_nick+" user_id: "+user_id);
                intent.putExtra("user_nick",user_nick);
                intent.putExtra("user_id",user_id);
                activity.startActivity(intent);
            }
        });

        roomHolder.sender_nick.setText(sender);
        roomHolder.Current_msg.setText(room.getLatestMessage());
        roomHolder.Current_time.setText(postControler.MessageTime_to_String(room.getLatestDate(),new Date()));
        if(count > 0){
            roomHolder.Letter_Count.setVisibility(View.VISIBLE);
            roomHolder.Letter_Count.setText(String.valueOf(count));
        }else
            roomHolder.Letter_Count.setVisibility(View.INVISIBLE);

    }

    @Override
    public int getItemCount() {
        return ChatRooms.size();
    }

}
