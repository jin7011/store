package com.example.sns_project.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_Letters;
import com.example.sns_project.data.LiveData_PostInfo;
import com.example.sns_project.databinding.ActivityChatRoomBinding;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.LetterInfo;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatRoomActivity extends AppCompatActivity {

    ActivityChatRoomBinding binding;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String receiver_nick;
    String receiver_id;
    String sender_nick;
    String sender_id;
    Date oldestDate; //메시지 보내는 시점이 될 것이고, 이것이 이후에 대화방을 나가던 아니던간에 이 시점까지만 메시지를 볼 수 있음. (대화방 나가면 나간 시점이 이 시점으로)
    String Pkey;
    String RoomKey;
    PostControler postControler = new PostControler();
    ArrayList<LetterInfo> letters;
    LiveData_Letters liveData_letters;
    My_Utility my_utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        liveData_letters = new ViewModelProvider(this).get(LiveData_Letters.class);
        liveData_letters.get().observe(this, new Observer<LetterInfo>() {
            @Override
            public void onChanged(LetterInfo letterInfo) {

            }
        });

        Get_Intent();
        SetDate();
    }

    private void Check_Room_Exist(){
        postControler.Get_Room(Pkey, new PostControler.Listener_Complete_Get_Room() {
            @Override
            public void onComplete_Get_Room(ChatRoomInfo room) {
                letters = Get_Letters_Before_Date(room.getLetters()); //저장된 기간까지의 대화내용만을 받아옴.
            }
            @Override
            public void onFail() {
                //todo 둘다 방을 나갈경우에 어떻게 방을 없앨지 생각해봐야함
                //방이 삭제된 방일경우 -> 위에 말한 todo와 연관이 있음. ( 아직 구현 x )
            }
        });
    }

    private ArrayList<LetterInfo> Get_Letters_Before_Date(ArrayList<LetterInfo> bring_letters){

        ArrayList<LetterInfo> NewLetters = new ArrayList<>();

        for(LetterInfo l : bring_letters){
            if(oldestDate.getTime() < l.getCreatedAt().getTime()){
                NewLetters.add(l);
            }
        }
        return NewLetters;
    }

    private void SetDate(){
        postControler.Find_Rooms(user.getUid(), new PostControler.Listener_Complete_Get_RoomsKey() {
            @Override
            public void onComplete_Get_RoomsKey(HashMap<String, Date> RoomsKey_Date) {
                //key존재하고 date있으면 거기까지만, 없으면 new date설정.
                if(RoomsKey_Date.containsKey(Pkey)){ //한번이라도 대화를 했던 방
                    oldestDate = RoomsKey_Date.get(Pkey);
                }else{ //처음 대화켜는 방
                    oldestDate = new Date();
                }
            }
        });
    }

    private void MakeKey(String receiver_id,String sender_id){
        Pkey = receiver_id.charAt(0) < sender_id.charAt(0) ? receiver_id+sender_id : sender_id+receiver_id;
    }

    private void Get_Intent(){
        Intent intent = getIntent();
        receiver_nick = intent.getStringExtra("receiver_nick");
        receiver_id = intent.getStringExtra("receiver_id");
        sender_nick = intent.getStringExtra("sender_nick");
        sender_id = intent.getStringExtra("sender_id");
        RoomKey = intent.getStringExtra("RoomKey");
        MakeKey(receiver_id,sender_id);
    }
}