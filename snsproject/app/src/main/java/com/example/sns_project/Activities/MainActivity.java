package com.example.sns_project.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_MyData_Main;
import com.example.sns_project.databinding.ActivityMainBinding;
import com.example.sns_project.fragment.BoardFragment;
import com.example.sns_project.fragment.LetterFragment;
import com.example.sns_project.fragment.ProfileFragment;
import com.example.sns_project.fragment.SearchFragment;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.info.PostInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private ArrayList<PostInfo> postList;
    private Toolbar toolbar;
    private LiveData_MyData_Main liveDataMyDataMainModel;
    private MyAccount myAccount;
    private BoardFragment boardFragment;
    private ProfileFragment profileFragment;
    private LetterFragment letterFragment;
    private SearchFragment searchFragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setToolbar();

        liveDataMyDataMainModel = new ViewModelProvider(MainActivity.this).get(LiveData_MyData_Main.class);
        liveDataMyDataMainModel.get().observe(this, new Observer<MyAccount>() {
            @Override
            public void onChanged(MyAccount myAccount) { //개인프로필을 변경했을 경우 -> 게시판 지역이동 (툴바이름변경,게시판내용변경)
                binding.setMyAccount(myAccount); //툴바에 해당 지역을 나타내는 textview를 데이터바인딩 하였음. (툴바이름변경)
                findLocation_and_setfragment(); //게시판 지역에 맞게 재설정 (게시판 내용변경)
            }
        });

        if(AccountInit()){ //계정이 있다면,
            findLocation_and_setfragment();
        }
        else{
            Activity(SignActivity.class);
        }

//        binding.button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FirebaseAuth.getInstance().signOut();
//                finish();
//            }
//        });

    }

    public void findLocation_and_setfragment(){

        DocumentReference locationDoc = db.collection("USER").document(user.getUid());

        locationDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) { //USER에서 location을 찾는 것은 비동기적이기떄문에 함수화 못했꼬, 그래서 우선적으로 지역을 찾자

                        location = document.getString("location"); //USER안에서 location을 찾아오는 쿼리(?)
                        Log.d("지격탐색(main)", location);
                        setFragment(location);
                    }
                }
            }
        });

    }

    public void setFragment(String location){

        boardFragment = new BoardFragment();

        Bundle bundle = new Bundle();
        bundle.putString("location",location);
        boardFragment.setArguments(bundle);
        //리퀘스트같은 번들내용을 줘서 frg에서 상황에 맞게 처리
        //글쓰기 -> 위로 갱신(새로고침도 위로갱신)
        //그냥 나옴 -> 전달 없이 finish
        //게시물삭제 -> 그냥갱신

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, boardFragment).commitAllowingStateLoss();

        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.menu_home:
                        if(boardFragment == null) {
                            boardFragment = new BoardFragment();
                            fragmentManager.beginTransaction().add(R.id.fragment_frame, boardFragment).commit();
                        }

                        if(boardFragment != null) fragmentManager.beginTransaction().show(boardFragment).commit();
                        if(profileFragment != null) fragmentManager.beginTransaction().hide(profileFragment).commit();
                        if(searchFragment != null) fragmentManager.beginTransaction().hide(searchFragment).commit();
                        if(letterFragment != null) fragmentManager.beginTransaction().hide(letterFragment).commit();
                        return true;
                    case R.id.menu_search:

                        if(searchFragment == null) {
                            searchFragment = new SearchFragment();
                            fragmentManager.beginTransaction().add(R.id.fragment_frame, searchFragment).commit();
                        }

                        if(searchFragment!= null) fragmentManager.beginTransaction().show(searchFragment).commit();
                        if(profileFragment != null) fragmentManager.beginTransaction().hide(profileFragment).commit();
                        if(boardFragment != null) fragmentManager.beginTransaction().hide(boardFragment).commit();
                        if(letterFragment != null) fragmentManager.beginTransaction().hide(letterFragment).commit();
                        return true;
                    case R.id.menu_letter:

                        if(letterFragment == null) {
                            letterFragment = new LetterFragment();
                            fragmentManager.beginTransaction().add(R.id.fragment_frame, letterFragment).commit();
                        }

                        if(letterFragment!= null) fragmentManager.beginTransaction().show(letterFragment).commit();
                        if(profileFragment != null) fragmentManager.beginTransaction().hide(profileFragment).commit();
                        if(searchFragment != null) fragmentManager.beginTransaction().hide(searchFragment).commit();
                        if(boardFragment!= null) fragmentManager.beginTransaction().hide(boardFragment).commit();
                        return true;
                    case R.id.menu_profile:

                        if(profileFragment == null) {
                            profileFragment = new ProfileFragment();
                            fragmentManager.beginTransaction().add(R.id.fragment_frame, profileFragment).commit();
                        }

                        if(profileFragment != null) fragmentManager.beginTransaction().show(profileFragment).commit();
                        if(boardFragment != null) fragmentManager.beginTransaction().hide(boardFragment).commit();
                        if(searchFragment != null) fragmentManager.beginTransaction().hide(searchFragment).commit();
                        if(letterFragment != null) fragmentManager.beginTransaction().hide(letterFragment).commit();
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    public boolean AccountInit() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user == null)
            return false;
        else {
            db.collection("USER").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();

                        String image = document.getString("image");
                        String location = document.getString("location");
                        String store = document.getString("store");
                        String phone = document.getString("phone");
                        String businessNum = document.getString("businessNum");
                        myAccount = new MyAccount(user.getUid(),user.getDisplayName(),image,location,store,phone,businessNum);
                        liveDataMyDataMainModel.get().setValue(myAccount);
                    }
                }
            });
            return true;
        }
    }

    public void setToolbar(){
        toolbar = (Toolbar)findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_main_write_post_btn:{
                Activity(WritePostActivity.class);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void Activity(Class c){
        Intent intent = new Intent(this,c);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101){
            Intent intent = getIntent();
            myAccount = (MyAccount)intent.getSerializableExtra("myAccount");
            Log.d("onActivity_main","myAccount location: "+myAccount.getLocation());
        }

        if (resultCode == 1) {
            Log.d("onActivity_main","requestCode: "+requestCode);
            boardFragment.postUpdate(1);
        }

        if (resultCode == 2) {
            Log.d("PostActivity","requestCode: "+requestCode);
            boardFragment.postUpdate(2);
        }

    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

}
