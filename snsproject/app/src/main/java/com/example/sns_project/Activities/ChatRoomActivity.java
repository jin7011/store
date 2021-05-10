package com.example.sns_project.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.example.sns_project.R;
import com.example.sns_project.databinding.ActivityChatRoomBinding;

public class ChatRoomActivity extends AppCompatActivity {

    ActivityChatRoomBinding binding;
    String receiver_

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}