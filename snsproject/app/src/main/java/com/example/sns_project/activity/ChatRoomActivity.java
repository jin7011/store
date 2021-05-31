package com.example.sns_project.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import com.example.sns_project.Adapter.LetterAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_Letters;
import com.example.sns_project.databinding.ActivityChatRoomBinding;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.LetterInfo;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.info.NotificationInfo;
import com.example.sns_project.util.My_Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.FIRST_BRING;
import static com.example.sns_project.util.Named.NEW_MESSAGE;
import static com.example.sns_project.util.Named.VERTICAL;

public class ChatRoomActivity extends AppCompatActivity {

    boolean isKeyboardShowing = false;
    int keypadBaseHeight = 0;

    ActivityChatRoomBinding binding;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String user_nick;
    String user_id;
    String my_nick;
    String my_id;
    Long My_OutTime; //메시지 보내는 시점이 될 것이고, 이것이 이후에 대화방을 나가던 아니던간에 이 시점까지만 메시지를 볼 수 있음. (대화방 나가면 나간 시점이 이 시점으로)
    String RoomKey;
    PostControler postControler = new PostControler();
    LiveData_Letters liveData_letters;
    My_Utility my_utility;
    LetterAdapter adapter;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipe;
    private Toolbar toolbar;
    private RelativeLayout loaderView;
    private ArrayList<LetterInfo> Loaded_Letters = new ArrayList<>();
    private boolean Done = false;
    private int Bring_Size = 0;
    private boolean FIRST_CHAT = false;
    private String my_token;
    private String user_token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        swipe = binding.SwipeLetters;
        loaderView = findViewById(R.id.loaderLyaout);

        liveData_letters = new ViewModelProvider(this).get(LiveData_Letters.class);
        liveData_letters.get().observe(this, new Observer<ArrayList<LetterInfo>>() {
            @Override
            public void onChanged(ArrayList<LetterInfo> Letters) {
                adapter.LetterInfoDiffUtil(Letters);
                if(NEW_MESSAGE || FIRST_BRING) {
                    recyclerView.scrollToPosition(Letters.size() - 1);
                    FIRST_BRING = false; NEW_MESSAGE = false;
                    Log.d("뭔가바뀜","올라가시라구요");
                }
                Log.d("뭔가바뀜",Letters.size()+"");
            }
        });

        Get_Intent();
        Set_Toolbar();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                my_token = task.getResult();
                FirebaseFirestore.getInstance().collection("USER").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        MyAccount user_Account = task.getResult().toObject(MyAccount.class);
                        user_token = user_Account.getToken();
                        Log.d("Room_token", "my: "+my_token+" others: "+user_token);

                        CreateRoom();
                        RecyclerViewInit(); //리사이클러뷰 셋팅해줘야함
                    }
                });
            }
        });

        binding.setChatRoomActivity(this);
    }

    private void CreateRoom() {
        postControler.Create_NewRoom(my_token,RoomKey, my_nick, my_id, user_token,user_nick, user_id, new PostControler.Listener_Check_Room() {
            @Override
            public void Done() {
                Set_Listener_And_Bring(); // 대화내용 있으면 -> 가져와서 셋하고 리스너달고 없으면 -> 리스너 바로 달고
                postControler.Set_Count_Zero(RoomKey,my_id);
            }
        });
    }

    private void RecyclerViewInit(){
        adapter = new LetterAdapter(this, new LetterAdapter.OnLoadMoreListener_top() {
            @Override
            public void onLoadMore() {
                if(liveData_letters.get().getValue() != null) {
                    ArrayList<LetterInfo> letters = new ArrayList<>(liveData_letters.get().getValue());

                    postControler.Bring_Letters(RoomKey, my_id,letters.get(0).getCreatedAt(), new PostControler.Listener_Complete_Get_Letters() {
                        @Override
                        public void onComplete_Get_Letters(ArrayList<LetterInfo> Letters) {
                            if(Letters.size() != 0) {
                                letters.addAll(0, Letters);
                                liveData_letters.get().setValue(letters);
                                adapter.Set_ReadMore(false);
                            }
                        }
                    });
                }
            }
        });
        recyclerView = binding.LettersRecyclerView;
        my_utility = new My_Utility(this,recyclerView,adapter);
        my_utility.RecyclerInit(VERTICAL);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////대화내용을 쓰기
    public void Send_Letter_btn(View view){
        Write_Letter();
        binding.AddLetterT.setText(null);
    }

    private void Write_Letter(){ //글을 보내는 입장
        String content = binding.AddLetterT.getText().toString();
        LetterInfo letter = new LetterInfo(user.getDisplayName(),user.getUid(),user_nick,user_id,content,new Date().getTime());

        ChatRoomInfo Room = new ChatRoomInfo(my_nick, my_id, new Date().getTime(), 0,my_token, user_nick, user_id, new Date().getTime(), 0,user_token, RoomKey,true);
        postControler.Update_letter(RoomKey,my_id,my_token,user_id,user_token,Room,letter);

        NotificationInfo noti = new NotificationInfo("님의 메시지가 도착했습니다.",user_token,user_nick,content,RoomKey,new Date().getTime());
        FirebaseFirestore.getInstance().collection("USER").document(user_id).collection("Notification").document(RoomKey).set(noti);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////대화내용을 읽기
    private void Set_Listener_And_Bring(){

        loaderView.setVisibility(View.VISIBLE);
        my_utility.Toast("대화내용을 불러오는 중입니다.");

        long latest_date = liveData_letters.get().getValue() == null ? new Date().getTime() : liveData_letters.get().getValue().get(liveData_letters.get().getValue().size()-1).getCreatedAt();

        postControler.Set_Listener_Letters(RoomKey, my_id, latest_date, new PostControler.Test() {
            @Override
            public void onComplete_Get_Letters(ArrayList<LetterInfo> Letters) {
                FIRST_BRING = true;
                liveData_letters.get().setValue(Letters);
                loaderView.setVisibility(View.GONE);
            }
            @Override
            public void NewLetters(LetterInfo letterInfo) {
                NEW_MESSAGE = true;
                ArrayList<LetterInfo> NewLetters = liveData_letters.get().getValue() == null ? new ArrayList<>() : new ArrayList<>(liveData_letters.get().getValue());
                NewLetters.add(letterInfo);
                liveData_letters.get().setValue(NewLetters);
            }
        });
    }

    private void MakeKey(String receiver_id,String sender_id){ //방의 기준으로 봤을 때에 id는 누구의 것도 아니기때문에 객관적인 기준으로 만들어야한다.
        RoomKey = receiver_id.charAt(0) < sender_id.charAt(0) ? receiver_id+sender_id : sender_id+receiver_id;
        Log.d("Pkey뽑음", RoomKey);
    }

    private void Get_Intent(){
        Intent intent = getIntent();
        user_nick = intent.getStringExtra("user_nick");
        user_id = intent.getStringExtra("user_id");
        my_id = user.getUid();
        my_nick = user.getDisplayName();

        MakeKey(user_id, my_id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(liveData_letters.get().getValue() != null && liveData_letters.get().getValue().size() != 0) {
            LetterInfo latest_letter = liveData_letters.get().getValue().get(liveData_letters.get().getValue().size()-1);
            postControler.Set_Before_Exit(RoomKey, my_id, latest_letter.getContents(), latest_letter.getCreatedAt()); //todo
        }else{
            postControler.Delete_Room(RoomKey,my_id);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("tmxkq","");
        if(liveData_letters.get().getValue() != null && liveData_letters.get().getValue().size() != 0) {
            LetterInfo latest_letter = liveData_letters.get().getValue().get(liveData_letters.get().getValue().size()-1);
            postControler.Set_Before_Exit(RoomKey, my_id, latest_letter.getContents(), latest_letter.getCreatedAt()); //todo
        }
    }

    public void Set_Toolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar_ChatRoom);
        setSupportActionBar(toolbar);
        binding.toolbarTitleChatRoom.setText(user_nick);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //커스텀툴바의 메뉴를 적용해주기
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chatroom_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.Out_Room:
                postControler.Delete_Room(RoomKey,my_id);
                finish();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //todo adjustPan하면 댓글창이 가려지고, adjustResize하면 리사이클러뷰가 안보이고 해서 생각해봐야하고, USER DB 생각해서 fragment만들어야함
//    public void asd(){
//
//        View rootView = binding.ChatReFrame;
//
//        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            public void onGlobalLayout() {
//                //뷰가 불러지고 나서 처리할 코드 입력
//                Rect r = new Rect(); // 키보드 위로 보여지는 공간
//                rootView.getWindowVisibleDisplayFrame(r);
//                int screenHeight = rootView.getRootView().getHeight(); // rootView에 대한 전체 높이
//
//                // 키보드가 보여지면 현재 보여지는 rootView의 범위가 전체 rootView의 범위보다 작아지므로 전체 크기에서 현재 보여지는 크기를 빼면 키보드의 크기가 됨.
//
//                int keypadHeight = screenHeight - r.bottom;
//                if (keypadBaseHeight == 0) { // 기기마다 소프트 키보드가 구현되는 방식이 다름. 화면 아래에 숨어있거나 invisible로 구현되어 있음. 그차이로 인해 기기마다 약간씩 레이아웃이 틀어지는데 그것을 방지하기 위해 필요함.
//                    keypadBaseHeight = keypadHeight;
//                }
//                if (keypadHeight > screenHeight * 0.15) { // 키보드가 대략 전체 화면의 15% 정도 높이 이상으로 올라온다.
//                    // 키보드 열렸을 때
//                    if (!isKeyboardShowing) {
//                        isKeyboardShowing = true;
////                        params.height = keypadHeight;
////                        onKeyboardVisibilityChanged(true);
//                        int height = keypadHeight - keypadBaseHeight;
//                        rootView.setMinimumHeight(height);
//                        Log.d("Asdzxc",keypadBaseHeight+"");
//                    }
//                } else {
//                    // 키보드가 닫혔을 때
//                    if (isKeyboardShowing) {
//                        isKeyboardShowing = false;
//                        rootView.setPadding(0, 0, 0, 0);
//                    }
//                }
//
//            }
//        });
//    }
}