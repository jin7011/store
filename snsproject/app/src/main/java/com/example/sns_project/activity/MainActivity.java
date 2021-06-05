package com.example.sns_project.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_MyData_Main;
import com.example.sns_project.databinding.ActivityMainBinding;
import com.example.sns_project.fragment.BoardFragment;
import com.example.sns_project.fragment.ChatRoomFragment;
import com.example.sns_project.fragment.ProfileFragment;
import com.example.sns_project.fragment.NotificationFragment;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import static com.example.sns_project.util.Named.CHANGED_LOCATION;
import static com.example.sns_project.util.Named.DELETE_RESULT;
import static com.example.sns_project.util.Named.BOARD_FRAGMENT;
import static com.example.sns_project.util.Named.LETTER_FRAGMENT;
import static com.example.sns_project.util.Named.NONE;
import static com.example.sns_project.util.Named.NOTIFICATION_FRAGMENT;
import static com.example.sns_project.util.Named.PROFILE_FRAGMENT;
import static com.example.sns_project.util.Named.SOMETHING_IN_POST;
import static com.example.sns_project.util.Named.WRITE_RESULT;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private long backKeyPressedTime = 0;
    private Toolbar toolbar;
    public LiveData_MyData_Main liveDataMyDataMainModel;
    private MyAccount myAccount;
    private BoardFragment boardFragment;
    private ProfileFragment profileFragment;
    private ChatRoomFragment chatRoomFragment;
    private NotificationFragment notificationFragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private PostControler postControler = new PostControler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(binding.getRoot());
        setToolbar(BOARD_FRAGMENT);

        liveDataMyDataMainModel = new ViewModelProvider(MainActivity.this).get(LiveData_MyData_Main.class);
        liveDataMyDataMainModel.get().observe(this, new Observer<MyAccount>() {
            @Override
            public void onChanged(MyAccount NewAccount) { //개인프로필을 변경했을 경우 -> 게시판 지역이동 (툴바이름변경,게시판내용변경)
                binding.setMyAccount(NewAccount); //툴바에 해당 지역을 나타내는 textview를 데이터바인딩 하였음. (툴바이름변경)
                myAccount = NewAccount;
                setFragment();
            }
        });

        Set_Account_First();
    }
    public void Set_Account_First(){
        if(user == null) {
            logout();
            return;
        }

        String id = user.getUid();

        postControler.Get_Account(id, new PostControler.Listener_Get_Account() {
            @Override
            public void Get_Account(MyAccount myAccount) {
                liveDataMyDataMainModel.get().setValue(myAccount);
                FirebaseMessaging.getInstance().subscribeToTopic(myAccount.getNoti() ? "total_noti" : "null"); //맨처음 로그인이라면 기본값이 true이고, 이후에 알림변경을 했다면, 저장된 값에 맞게
//                FirebaseMessaging.getInstance().subscribeToTopic(PreferenceManager.getBoolean(getApplicationContext(),"Post_noti") ? "Post_noti" : "null");
                FirebaseMessaging.getInstance().subscribeToTopic(PreferenceManager.getBoolean(getApplicationContext(),"ChatRoom_noti") ? "Message"+myAccount.getId() : "null");
                FirebaseMessaging.getInstance().subscribeToTopic(PreferenceManager.getBoolean(getApplicationContext(),"Comment_noti")? "Comment"+myAccount.getId() : "null");
                FirebaseMessaging.getInstance().subscribeToTopic(PreferenceManager.getBoolean(getApplicationContext(),"Recomment_noti")? "Recomment"+myAccount.getId() : "null");
            }
        });
    }

    public void setFragment(){

        boardFragment = null;
        chatRoomFragment = null;
        profileFragment = null;
        notificationFragment = null;

        boardFragment = new BoardFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("Myaccount",myAccount);
        boardFragment.setArguments(bundle);
        //리퀘스트같은 번들내용을 줘서 frg에서 상황에 맞게 처리
        //글쓰기 -> 위로 갱신(새로고침도 위로갱신)
        //그냥 나옴 -> 전달 없이 finish
        //게시물삭제 -> 그냥갱신

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, boardFragment).commitAllowingStateLoss();
        binding.bottomNav.setSelectedItemId(R.id.menu_home);

        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.menu_home:
                        setToolbar(BOARD_FRAGMENT);

                        if(boardFragment == null) {
                            boardFragment = new BoardFragment();
                            boardFragment.setArguments(bundle);
                            fragmentManager.beginTransaction().add(R.id.fragment_frame, boardFragment).commit();
                        }

                        if(boardFragment != null) fragmentManager.beginTransaction().show(boardFragment).commit();
                        if(profileFragment != null) fragmentManager.beginTransaction().hide(profileFragment).commit();
                        if(notificationFragment != null) fragmentManager.beginTransaction().hide(notificationFragment).commit();
                        if(chatRoomFragment != null) fragmentManager.beginTransaction().hide(chatRoomFragment).commit();
                        return true;

                    case R.id.menu_notification:
                        setToolbar(NOTIFICATION_FRAGMENT);

                        if(notificationFragment == null) {
                            notificationFragment = new NotificationFragment();
                            notificationFragment.setArguments(bundle);
                            fragmentManager.beginTransaction().add(R.id.fragment_frame, notificationFragment).commit();
                        }

                        if(notificationFragment != null) fragmentManager.beginTransaction().show(notificationFragment).commit();
                        if(profileFragment != null) fragmentManager.beginTransaction().hide(profileFragment).commit();
                        if(boardFragment != null) fragmentManager.beginTransaction().hide(boardFragment).commit();
                        if(chatRoomFragment != null) fragmentManager.beginTransaction().hide(chatRoomFragment).commit();
                        return true;

                    case R.id.menu_letter:
                        setToolbar(LETTER_FRAGMENT);

                        if(chatRoomFragment == null) {
                            chatRoomFragment = new ChatRoomFragment();
                            chatRoomFragment.setArguments(bundle);
                            fragmentManager.beginTransaction().add(R.id.fragment_frame, chatRoomFragment).commit();
                        }

                        if(chatRoomFragment != null) fragmentManager.beginTransaction().show(chatRoomFragment).commit();
                        if(profileFragment != null) fragmentManager.beginTransaction().hide(profileFragment).commit();
                        if(notificationFragment != null) fragmentManager.beginTransaction().hide(notificationFragment).commit();
                        if(boardFragment!= null) fragmentManager.beginTransaction().hide(boardFragment).commit();
                        return true;

                    case R.id.menu_profile:
                        setToolbar(PROFILE_FRAGMENT);

                        if(profileFragment == null) {
                            profileFragment = new ProfileFragment();
                            profileFragment.setArguments(bundle);
                            fragmentManager.beginTransaction().add(R.id.fragment_frame, profileFragment).commit();
                        }

                        if(profileFragment != null) fragmentManager.beginTransaction().show(profileFragment).commit();
                        if(boardFragment != null) fragmentManager.beginTransaction().hide(boardFragment).commit();
                        if(notificationFragment != null) fragmentManager.beginTransaction().hide(notificationFragment).commit();
                        if(chatRoomFragment != null) fragmentManager.beginTransaction().hide(chatRoomFragment).commit();
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    public void setToolbar(int FragmentStatus){ //기본툴바를 커스텀으로

        if(FragmentStatus == BOARD_FRAGMENT) {
            toolbar = (Toolbar) findViewById(R.id.toolbar_main);
            setSupportActionBar(toolbar);
            binding.toolbarTitle.setVisibility(View.VISIBLE);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        if(FragmentStatus == NOTIFICATION_FRAGMENT) {
            toolbar.getMenu().clear(); //오른쪽 inflated 메뉴 없애줌.
            binding.toolbarTitle.setVisibility(View.GONE); //바인드되어있는 지역표시 가려줌
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("알림내용");
        }

        if(FragmentStatus == LETTER_FRAGMENT) {
            toolbar.getMenu().clear();
            binding.toolbarTitle.setVisibility(View.GONE);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("대화내용");
        }

        if(FragmentStatus == PROFILE_FRAGMENT) {
            toolbar.getMenu().clear();
            binding.toolbarTitle.setVisibility(View.GONE);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("내 정보");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Check_Account_Changed();
        Log.d("resume","");
    }

    public void Check_Account_Changed() {
        // Initialize Firebase Auth

        if (user == null) //자동로그인 확인
        {
            Activity(SignActivity.class);
        }
        else {
            postControler.Get_Account(user.getUid(), new PostControler.Listener_Get_Account() {
                @Override
                public void Get_Account(MyAccount NewAccount) {

                    if(NewAccount == null) //어떠한 이유에서 존재하지 않는 계정이라면 로그아웃상태로
                        logout();

                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            String token = task.getResult();
                            String location = NewAccount.getLocation();
                            Boolean Notification = NewAccount.getNoti();

                            if(myAccount == null || !myAccount.getLocation().equals(location) || !myAccount.getNoti().equals(Notification) || !myAccount.getToken().equals(token)){
                                Log.d("AccountChanged",NewAccount.getLocation());
                                liveDataMyDataMainModel.get().setValue(NewAccount);
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //커스텀툴바의 메뉴를 적용해주기
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //툴바의 메뉴이벤트 적용
        switch (item.getItemId()){
            case R.id.toolbar_main_write_post_btn:{
                Activity(WritePostActivity.class);
                break;
            }
            case R.id.toolbar_main_search:{
                if(myAccount != null)
                    Activity(SearchActivity.class,myAccount.getLocation());
                break;
            }
            case R.id.toolbar_main_reset:{
                item.setEnabled(false);
                boardFragment.UpScrolled();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        item.setEnabled(true);
                    }
                }, 1500); //딜레이 타임 조절
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void Activity(Class c){
        Intent intent = new Intent(this,c);
        startActivityForResult(intent,1);
    }

    public void Activity(Class c,String location){
        Intent intent = new Intent(this,c);
        intent.putExtra("location",location);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101){
            Intent intent = getIntent();
            myAccount = (MyAccount)intent.getParcelableExtra("myAccount");
            Log.d("From SignActivity","myAccount location: "+myAccount.getLocation());
        }

        if (resultCode == WRITE_RESULT) { //글쓰기 리턴값
            Log.d("From WriteActivity","requestCode: "+requestCode);
            boardFragment.PostUpdate(WRITE_RESULT, NONE);
        }

        if (resultCode == DELETE_RESULT) { //글삭제 리턴값
            String docid = null;
            if (data != null) {
                docid = data.getStringExtra("docid");
            }
            Log.d("From PostActivity","requestCode: "+requestCode+" docid: "+docid);
            boardFragment.PostUpdate(DELETE_RESULT,docid);
        }

        if (resultCode == SOMETHING_IN_POST) { //좋아요/(댓글추가) 리턴값
            String docid = null;
            if (data != null) {
                docid = data.getStringExtra("docid");
            }
            Log.d("From PostActivity","requestCode: "+requestCode+" docid: "+docid);
            boardFragment.PostUpdate(SOMETHING_IN_POST,docid);
        }

        if(resultCode == WRITE_RESULT){ //지역변경 리턴값
            Check_Account_Changed();
            Log.d("From PopupAct","myAccount location: "+myAccount.getLocation());
        }

        if(requestCode == CHANGED_LOCATION){
            Check_Account_Changed();
            Log.d("CHANGED_LOCATION","myAccount location: "+myAccount.getLocation());
        }

    }

    @Override
    public void onBackPressed() {
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
        if (System.currentTimeMillis() > backKeyPressedTime + 1500) {
            backKeyPressedTime = System.currentTimeMillis();
            Tost("\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.");
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 1500) {
            //아래 3줄은 프로세스 종료
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    public void logout(){
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        FirebaseAuth.getInstance().signOut();
        myAccount = null;
        startActivity(i);
        finish();
    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

}
