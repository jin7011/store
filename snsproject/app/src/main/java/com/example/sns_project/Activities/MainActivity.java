package com.example.sns_project.Activities;

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
import com.example.sns_project.fragment.LetterFragment;
import com.example.sns_project.fragment.ProfileFragment;
import com.example.sns_project.fragment.NotificationFragment;
import com.example.sns_project.info.MyAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

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
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private long backKeyPressedTime = 0;
    private Toolbar toolbar;
    public LiveData_MyData_Main liveDataMyDataMainModel;
    private MyAccount myAccount;
    private BoardFragment boardFragment;
    private ProfileFragment profileFragment;
    private LetterFragment letterFragment;
    private NotificationFragment notificationFragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private PostControler postControler;

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
            public void onChanged(MyAccount myAccount) { //개인프로필을 변경했을 경우 -> 게시판 지역이동 (툴바이름변경,게시판내용변경)
                binding.setMyAccount(myAccount); //툴바에 해당 지역을 나타내는 textview를 데이터바인딩 하였음. (툴바이름변경)
                setFragment();
            }
        });

        if(AccountInit()){}
        else{Activity(SignActivity.class);}

    }

    public void setFragment(){

        boardFragment = null;
        letterFragment = null;
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
                        if(letterFragment != null) fragmentManager.beginTransaction().hide(letterFragment).commit();
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
                        if(letterFragment != null) fragmentManager.beginTransaction().hide(letterFragment).commit();
                        return true;

                    case R.id.menu_letter:
                        setToolbar(LETTER_FRAGMENT);

                        if(letterFragment == null) {
                            letterFragment = new LetterFragment();
                            letterFragment.setArguments(bundle);
                            fragmentManager.beginTransaction().add(R.id.fragment_frame, letterFragment).commit();
                        }

                        if(letterFragment!= null) fragmentManager.beginTransaction().show(letterFragment).commit();
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
                        if(letterFragment != null) fragmentManager.beginTransaction().hide(letterFragment).commit();
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

        setToolbar(BOARD_FRAGMENT);

        if(AccountInit()){ //계정이 있다면,
            Log.d("resume_accountinit(): ",user.getEmail());
        }
        else{
            Activity(SignActivity.class);
        }
    }

    public boolean AccountInit() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user == null) //자동로그인 확인
            return false;
        else {
            db.collection("USER").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot document) {
                    if(document.exists()) {
                        String location = document.getString("location");
                        String image = document.getString("image");
                        String store = document.getString("store");
                        String phone = document.getString("phone");
                        String businessNum = document.getString("businessNum");

                        postControler = new PostControler(location);

                        Log.d("dasdazz",location);
                        if(myAccount == null) {
                            //처음 어플 켰을 때
                            Log.d("dasdazz","null"+location);
                            myAccount = new MyAccount(user.getUid(), user.getDisplayName(), image, location, store, phone, businessNum,postControler.Get_Rooms_From_Store(document));
                            liveDataMyDataMainModel.get().setValue(myAccount);
                        }else if(location != null && !myAccount.getLocation().equals(location)){
                            //지역변경을 하고 왔을 때의 처리
                            Log.d("dasdazz","not_null: "+myAccount.getLocation()+", new: "+location);
                            myAccount = new MyAccount(user.getUid(), user.getDisplayName(), image, location, store, phone, businessNum,postControler.Get_Rooms_From_Store(document));
                            liveDataMyDataMainModel.get().setValue(myAccount);
                        }

                        if(location == null){
                            //비정상적인 경로임. auth에는 계정이 남아있고, user 스토리지에는 계정이 안지워진 상태.
                            Log.d("dasdazz","스토리지 널");
                            logout();
                        }
                    }else{
                        logout();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("cccccccccaa","실패");
                }
            });

            return true;
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
            AccountInit();
            Log.d("From PopupAct","myAccount location: "+myAccount.getLocation());
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
        mAuth.signOut();
        myAccount = null;
        startActivity(i);
        finish();
    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

}
