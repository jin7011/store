package com.example.sns_project.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.sns_project.Adapter.LetterAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.data.LiveData_Letters;
import com.example.sns_project.databinding.ActivityChatRoomBinding;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.LetterInfo;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static com.example.sns_project.util.Named.VERTICAL;

public class ChatRoomActivity extends AppCompatActivity {

    ActivityChatRoomBinding binding;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String user_nick;
    String user_id;
    String my_nick;
    String my_id;
    Date My_OutTime; //메시지 보내는 시점이 될 것이고, 이것이 이후에 대화방을 나가던 아니던간에 이 시점까지만 메시지를 볼 수 있음. (대화방 나가면 나간 시점이 이 시점으로)
    Date User_OutTime;
    String RoomKey;
    PostControler postControler = new PostControler();
    LiveData_Letters liveData_letters;
    My_Utility my_utility;
    Boolean FIRST_ROOM;
    LetterAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        liveData_letters = new ViewModelProvider(this).get(LiveData_Letters.class);
        liveData_letters.get().observe(this, new Observer<ChatRoomInfo>() {
            @Override
            public void onChanged(ChatRoomInfo NewRoom) {
                FIRST_ROOM = false;
                ArrayList<LetterInfo> NewLetters = Get_Letters_Before_Date(NewRoom.getLetters());
                adapter.LetterInfoDiffUtil(NewLetters);
                recyclerView.scrollToPosition(NewLetters.size()-1);
                Log.d("뭔가바뀜",NewLetters.size()+"");
            }
        });

        Get_Intent();
        Set_Room_ChangeListner(); //이후에 변경되는 내용을 리스너로 관리
        RecyclerViewInit(); //리사이클러뷰 셋팅해줘야함
        binding.setChatRoomActivity(this);
    }

    private void RecyclerViewInit(){
        adapter = new LetterAdapter(this);
        recyclerView = binding.LettersRecyclerView;
        my_utility = new My_Utility(this,recyclerView,adapter);
        my_utility.RecyclerInit(VERTICAL);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////대화내용을 쓰기
    public void Send_Letter_btn(View view){
        Write_Letter();
        binding.AddLetterT.setText(null);
        Log.d("qhsor","보내기 눌림 : " +FIRST_ROOM);
    }

    private void Write_Letter(){ //글을 보내는 입장
        String content = binding.AddLetterT.getText().toString();
        LetterInfo letter = new LetterInfo(user.getDisplayName(),user.getUid(),user_nick,user_id,content,new Date());
        ChatRoomInfo room;

        if(FIRST_ROOM){
            Date date = new Date();
            ArrayList<LetterInfo> Letters = new ArrayList<>();
            Letters.add(letter);
            room = new ChatRoomInfo(my_nick,my_id,date,user_nick,user_id,date,date,Letters, RoomKey);
            Log.d("처음",content);
        }else{
            ArrayList<LetterInfo> Letters = postControler.deepCopy_Letters(Objects.requireNonNull(liveData_letters.get().getValue()).getLetters());
            room = new ChatRoomInfo(Objects.requireNonNull(liveData_letters.get().getValue()));
            Letters.add(letter);
            room.setLetters(Letters);
            Log.d("처음아님",Letters.size()+"");
        }

        postControler.Set_Room(room, RoomKey, new PostControler.Listener_Complete_Set_Room() {
            @Override
            public void onComplete_Set_Room() {
                if(FIRST_ROOM){
                    Set_Room_ChangeListner();
                }
            }
        });
    }
    //////////////////////////////////////////////////////////////////////////////////////////////대화내용을 읽기
    private ArrayList<LetterInfo> Get_Letters_Before_Date(ArrayList<LetterInfo> bring_letters){

        ArrayList<LetterInfo> NewLetters = new ArrayList<>();

        if(My_OutTime != null) {
            for (LetterInfo l : bring_letters) {
                if (My_OutTime.getTime() < l.getCreatedAt().getTime()) {
                    NewLetters.add(l);
                }
            }
            Log.d("레터뽑음",""+NewLetters.size());
        }else{
            Log.d("레터뽑음",""+bring_letters.size());

            return bring_letters;
        }

        return NewLetters;
    }

    private void Set_Room_ChangeListner(){
        postControler.Set_Room_Listener(RoomKey, new PostControler.Listener_Room_Changed() {
            @Override
            public void onChanged_Room(ChatRoomInfo room) {
                liveData_letters.get().setValue(room);
            }
            @Override
            public void onFail() { FIRST_ROOM = true; }
        });
    }

    private void Set_OutTime(ChatRoomInfo room){
        String user1_ID = room.getUser1_id();
        My_OutTime = user1_ID.equals(my_id) ? room.getUser1_OutDate() : room.getUser2_OutDate();
    }

    private void MakeKey(String receiver_id,String sender_id){ //방의 기준으로 봤을 때에 id는 누구의 것도 아니기때문에 객관적인 기준으로 만들어야한다.
        RoomKey = receiver_id.charAt(0) < sender_id.charAt(0) ? receiver_id+sender_id : sender_id+receiver_id;
        Log.d("Pkey뽑음", RoomKey);
    }

    private void Get_Intent(){
        Intent intent = getIntent();
        user_nick = intent.getStringExtra("receiver_nick");
        user_id = intent.getStringExtra("receiver_id");
        my_id = user.getUid();
        my_nick = user.getDisplayName();
        MakeKey(user_id, my_id);
    }

//    private void SetDate(){
//        postControler.Get_Room_From_Store(RoomKey, new PostControler.Listener_Complete_Get_Room() {
//            @Override
//            public void onComplete_Get_Room(ChatRoomInfo room) { //대화했던 방 -> 방 가져왔음.
//                Set_OutTime(room);
//                liveData_letters.get().setValue(room);
//                Log.d("SetDate()", "처음아님:"+room);
//            }
//            @Override
//            public void onFail() { //처음 대화함 -> 글 올릴때 방파야댐
//                Log.d("SetDate()", "처음");
//                FIRST_ROOM = true;
//            }
//        });
//    }

    public void hideKeyPad(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.AddLetterT.getWindowToken(), 0);
    }

}