package com.example.sns_project.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.sns_project.Adapter.LetterAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_Letters;
import com.example.sns_project.databinding.ActivityChatRoomBinding;
import com.example.sns_project.info.LetterInfo;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;

import static com.example.sns_project.util.Named.CREATE;
import static com.example.sns_project.util.Named.FIRST_BRING;
import static com.example.sns_project.util.Named.NEW_MESSAGE;
import static com.example.sns_project.util.Named.UPLOAD_LIMIT;
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
    private RelativeLayout loaderView;
    private ArrayList<LetterInfo> Loaded_Letters = new ArrayList<>();
    private int Loaded_IDX = UPLOAD_LIMIT+1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        swipe = binding.SwipeLetters;
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

//        asd();
        Get_Intent();
        CreateRoom();
        RecyclerViewInit(); //리사이클러뷰 셋팅해줘야함
        Set_Swipe();
        binding.setChatRoomActivity(this);
    }

    //todo 기기마다 시간차이 심해서 서버타임써야하는데 그것도 쓰기 복잡하고 파베에서 지원하는 쿼리 도저히 못써먹겠어서 그냥 통째로 메시지 들고 올거임.

    private void CreateRoom() {
        postControler.Create_NewRoom(RoomKey, my_nick, my_id, user_nick, user_id, new PostControler.Listener_Room_Outdate() {
            @Override
            public void GetOutdate_Room(Long OutDate) {
                My_OutTime = OutDate;
                Log.d("myoutdate",My_OutTime + "");
            }

            @Override
            public void Done() {
                Bring_letters(); // 대화내용 있으면 -> 가져와서 셋하고 리스너달고 없으면 -> 리스너 바로 달고
                Set_Listener();
                postControler.Set_Count_Zero(RoomKey,my_id);
            }
        });

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
    }

    private void Write_Letter(){ //글을 보내는 입장
        String content = binding.AddLetterT.getText().toString();
        LetterInfo letter = new LetterInfo(user.getDisplayName(),user.getUid(),user_nick,user_id,content,new Date().getTime());

        postControler.Update_letter(RoomKey,letter);
        postControler.Set_LatestMessage(RoomKey,content,new Date().getTime());
        postControler.Set_Count_UP(RoomKey);

        if(liveData_letters.get().getValue() == null){
            postControler.Set_RoomKey_User(my_id,RoomKey,CREATE); //처음 메시지를 보낸다면 보낸 시점부터는 실제 유저데이터(store)에 채팅방의 키가 기록으로 남겨짐
            postControler.Set_RoomKey_User(user_id,RoomKey,CREATE);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////대화내용을 읽기
    private void Bring_letters(){

        loaderView.setVisibility(View.VISIBLE);
        my_utility.Toast("대화내용을 불러오는 중입니다.");

        postControler.Bring_Letters(RoomKey,liveData_letters.get().getValue(),new PostControler.Listener_Complete_Get_Letters() {
            @Override
            public void onComplete_Get_Letters(ArrayList<LetterInfo> Letters) { //이미 존재하면 대화내역 불러오기
                Loaded_Letters = Get_Letters_Before_Date(Letters);
                ArrayList<LetterInfo> BringLetters = new ArrayList<>();

                Log.d("NEWpdfpf",Loaded_Letters.get(0).getContents()+"");
                Log.d("NEWpdfpf",Loaded_Letters.get(Loaded_Letters.size()-1).getContents()+"");

                if(liveData_letters.get().getValue() == null) FIRST_BRING = true;

                for(int x=0; x<=UPLOAD_LIMIT; x++){
                    BringLetters.add(Loaded_Letters.get(x));
                }

                liveData_letters.get().setValue(BringLetters);
                loaderView.setVisibility(View.GONE);
                //todo 가져오기는 싹 가져오고 전역변수로 놓고 스크롤링하면서 20개씩 바인드시키자.
            }
            @Override
            public void onFail() {} //없으면 말구
        });
    }

    private void Set_Listener(){
        postControler.Set_RealtimeListener_onLetters(RoomKey, new PostControler.Listener_NewLetter() {
            @Override
            public void Listener_NewLetter(LetterInfo Letter) {
                ArrayList<LetterInfo> NewLetters = new ArrayList<>();

                if(liveData_letters.get().getValue() != null)
                    NewLetters = new ArrayList<>(liveData_letters.get().getValue());

                NewLetters.add(Letter);
                Loaded_Letters.add(Letter); //처음 가져온 전체메시지도 최신화 시켜줌
                NEW_MESSAGE = true;
                liveData_letters.get().setValue(NewLetters);
            }
        });
    }

    private void MakeKey(String receiver_id,String sender_id){ //방의 기준으로 봤을 때에 id는 누구의 것도 아니기때문에 객관적인 기준으로 만들어야한다.
        RoomKey = receiver_id.charAt(0) < sender_id.charAt(0) ? receiver_id+sender_id : sender_id+receiver_id;
        Log.d("Pkey뽑음", RoomKey);
    }

    private ArrayList<LetterInfo> Get_Letters_Before_Date(ArrayList<LetterInfo> bring_letters){

        ArrayList<LetterInfo> NewLetters = new ArrayList<>();

        if(My_OutTime != null) {
            for (LetterInfo l : bring_letters) {
                if (My_OutTime < l.getCreatedAt()) {
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
        Log.d("emtmxm","");
        if(liveData_letters.get().getValue() == null) {
            postControler.Delete_Room(RoomKey);
        }
        else {
            LetterInfo latest_letter = liveData_letters.get().getValue().get(liveData_letters.get().getValue().size()-1);
            postControler.Set_Before_Exit(RoomKey, my_id, latest_letter.getContents(), latest_letter.getCreatedAt());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("tmxkq","");
        if(liveData_letters.get().getValue() != null) {
            LetterInfo latest_letter = liveData_letters.get().getValue().get(liveData_letters.get().getValue().size()-1);
            postControler.Set_Before_Exit(RoomKey, my_id, latest_letter.getContents(), latest_letter.getCreatedAt());
        }
    }

    public void Set_Swipe() {
        swipe.setColorSchemeResources(R.color.classicBlue);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        //swipe 업데이트는 다르게 해줄 필요가 있는게 당기면 위로 정보가 업데이트 되어야 하는데 지금같은 경운 date를 기반으로 아래로 새로고침되니까 새로운 글이 나오지않음
//                        //swipe를 할경우 기존의 List의 뒷부분은 다 지우고 앞부분부터 새로 받아와야함
//                        //반면에 삭제는 가지고 있는 리스트를 그대로 유지하고 내가 쓴 글의 position만 지우고 갱신함.
//                        Bring_letters();
                        ArrayList<LetterInfo> BringLetters = new ArrayList<>();

                        for(int x = Loaded_IDX; x < Loaded_IDX+UPLOAD_LIMIT; x++){// 21~40
                            if(Loaded_Letters.get(x) != null){
                                BringLetters.add(Loaded_Letters.get(x));
                            }else
                                break;

                            if(x == Loaded_IDX+UPLOAD_LIMIT-1){ //마지막까지 로드가 되었다면, (이후에 추가로 더 로드할게 있거나, 여기꺼지거나)
                                Loaded_IDX += 20;
                                break;
                            }
                        }

                        ArrayList<LetterInfo> NewLetter = new ArrayList<>(BringLetters);
                        NewLetter.addAll(liveData_letters.get().getValue());
                        Log.d("djWLehkTSk","idx: "+Loaded_IDX+" bring_size: "+BringLetters.size() + " New size: "+NewLetter.size());
                        liveData_letters.get().setValue(NewLetter);
                        swipe.setRefreshing(false);
                    }
                }, 500);
            }
        });
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