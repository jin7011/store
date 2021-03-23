package com.example.sns_project.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sns_project.Info.PostInfo;
import com.example.sns_project.databinding.ActivityPostBinding;

public class PostActivity extends AppCompatActivity {
    ActivityPostBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PostInfo postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
        String content = postInfo.getContents();


    }


}