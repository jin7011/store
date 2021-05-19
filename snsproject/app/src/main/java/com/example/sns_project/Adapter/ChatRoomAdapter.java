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
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.activity.ChatRoomActivity;
import com.example.sns_project.activity.MainActivity;
import com.example.sns_project.activity.PostActivity;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.LetterInfo;
import com.example.sns_project.util.ChatRoomInfo_DiffUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.CustomLibrary.PostControler.MessageTime_to_String;
import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.SEC;

public class ChatRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final Activity activity;
    private final PostControler postControler;
    private final ArrayList<ChatRoomInfo> ChatRooms;
    private Listener_NewMessage listener_newMessage;

    public interface Listener_NewMessage{
        void onNewMessage(ChatRoomInfo room,int position);
    }

    public void ChatRoomInfo_DiffUtile(ArrayList<ChatRoomInfo> NewChatRooms) {
        final ChatRoomInfo_DiffUtil diffCallback = new ChatRoomInfo_DiffUtil(this.ChatRooms, NewChatRooms);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.ChatRooms.clear();
        this.ChatRooms.addAll(NewChatRooms);

        diffResult.dispatchUpdatesTo(this);
    }

    public ChatRoomAdapter(Activity activity,Listener_NewMessage listener_newMessage) {
        this.activity = activity;
        this.ChatRooms = new ArrayList<>();
        this.postControler = new PostControler();
        this.listener_newMessage = listener_newMessage;
    }

    //holder
    public static class Room_Holder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        //R.layout.item_comments에 존재하지 않는 뷰는 일반적으로는 설정하나마나임
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
        ChatRoomAdapter.Room_Holder roomHolder = new ChatRoomAdapter.Room_Holder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatRoomInfo room = ChatRooms.get(roomHolder.getAbsoluteAdapterPosition());
                String user_nick = room.getUser1_id().equals(user.getUid()) ? room.getUser2() : room.getUser1();
                String user_id =  room.getUser1_id().equals(user.getUid()) ? room.getUser2_id() : room.getUser1_id();
                Intent intent = new Intent(activity, ChatRoomActivity.class);
                Log.d("dnwjdkel","user_nick: " + user_nick+" user_id: "+user_id);
                intent.putExtra("user_nick",user_nick);
                intent.putExtra("user_id",user_id);
                activity.startActivity(intent);
            }
        });

        return roomHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Room_Holder roomHolder = (Room_Holder)holder;
        ChatRoomInfo room = ChatRooms.get(position);
        String sender = room.getUser1_id().equals(user.getUid()) ? room.getUser2() : room.getUser1();
        int count = room.getUser1_id().equals(user.getUid()) ? room.getUser1_count() : room.getUser2_count();

        postControler.Listener_Room(room.getKey(), new PostControler.Listener_Get_Room() {
            @Override
            public void onGetRoom(ChatRoomInfo room) {
                Log.d("dhkTEk",room.getLatestMessage()+"");
                listener_newMessage.onNewMessage(room,position);
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
