package com.example.sns_project.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
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

import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.SEC;

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

    public ChatRoomAdapter(PostActivity activity) {
        this.activity = activity;
        this.ChatRooms = new ArrayList<>();
        this.postControler = new PostControler();
    }

    //holder
    public static class Room_Holder extends RecyclerView.ViewHolder { //홀더에 담고싶은 그릇(이미지뷰)를 정함
        //R.layout.item_comments에 존재하지 않는 뷰는 일반적으로는 설정하나마나임
        TextView sender_nick ;
        TextView Current_time ;
        TextView Current_msg;

        public Room_Holder (@NonNull View itemView) {
            super(itemView);
            sender_nick = itemView.findViewById(R.id.Room_nicknameT);
            Current_time = itemView.findViewById(R.id.Room_dateT);
            Current_msg = itemView.findViewById(R.id.Room_contentT);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        ChatRoomAdapter.Room_Holder roomHolder = new ChatRoomAdapter.Room_Holder(view);

        return roomHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Room_Holder roomHolder = (Room_Holder)holder;
        LetterInfo letterInfo = ChatRooms.get(position).getLetters().get(ChatRooms.get(position).getLetters().size()-1);
        String Current_msg = letterInfo.getContents();
        String Current_time = formatTimeString(letterInfo.getCreatedAt(),new Date());
        String sender_nick = letterInfo.getSender_nick();

        roomHolder.Current_msg.setText(Current_msg);
        roomHolder.sender_nick.setText(sender_nick);
        roomHolder.Current_time.setText(Current_time);

    }

    @Override
    public int getItemCount() {
        return ChatRooms.size();
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
