package com.example.sns_project.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CompoundButton;

import com.example.sns_project.R;
import com.example.sns_project.databinding.ActivityCheckNotificationBinding;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.util.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class Check_NotificationActivity extends AppCompatActivity {

    ActivityCheckNotificationBinding binding;
    MyAccount myAccount;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore store = FirebaseFirestore.getInstance();
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = getIntent().getExtras();
        myAccount = bundle.getParcelable("Myaccount");

        binding.totalNotiChek.setChecked(myAccount.getNoti()); //전체 설정은 db에서 받아오기로. 짜잘한건 그냥 프리퍼런스로
//        binding.postNotiChek.setChecked(PreferenceManager.getBoolean(this,"Post_noti"));
        binding.ChatRoomNotiChek.setChecked(PreferenceManager.getBoolean(this,"ChatRoom_noti"));
        binding.CommentNotiChek.setChecked(PreferenceManager.getBoolean(this,"Comment_noti"));
        binding.RecommentNotiChek.setChecked(PreferenceManager.getBoolean(this,"Recomment_noti"));
        PreferenceManager.setBoolean(this,"total_noti",myAccount.getNoti());

        binding.totalNotiChek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    FirebaseFirestore.getInstance().collection("USER").document(myAccount.getId()).update("noti",true);
//                    FirebaseMessaging.getInstance().subscribeToTopic("total_noti");
                    PreferenceManager.setBoolean(getApplicationContext(),"total_noti",true);
                }else{
                    FirebaseFirestore.getInstance().collection("USER").document(myAccount.getId()).update("noti",false);
//                    FirebaseMessaging.getInstance().unsubscribeFromTopic("total_noti");
                    PreferenceManager.setBoolean(getApplicationContext(),"total_noti",false);
                }
            }
        });

        binding.ChatRoomNotiChek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    FirebaseMessaging.getInstance().subscribeToTopic("Message"+myAccount.getId());
                    PreferenceManager.setBoolean(getApplicationContext(),"ChatRoom_noti",true);
                }else{
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("Message"+myAccount.getId());
                    PreferenceManager.setBoolean(getApplicationContext(),"ChatRoom_noti",false);
                }
            }
        });

        binding.CommentNotiChek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    FirebaseMessaging.getInstance().subscribeToTopic("Comment"+myAccount.getId());
                    PreferenceManager.setBoolean(getApplicationContext(),"Comment_noti",true);
                }else{
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("Comment"+myAccount.getId());
                    PreferenceManager.setBoolean(getApplicationContext(),"Comment_noti",false);
                }
            }
        });

        binding.RecommentNotiChek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    FirebaseMessaging.getInstance().subscribeToTopic("Recomment"+myAccount.getId());
                    PreferenceManager.setBoolean(getApplicationContext(),"Recomment_noti",true);
                }else{
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("Recomment"+myAccount.getId());
                    PreferenceManager.setBoolean(getApplicationContext(),"Recomment_noti",false);
                }
            }
        });
    }
}