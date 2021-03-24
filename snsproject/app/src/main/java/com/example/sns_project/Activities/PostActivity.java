package com.example.sns_project.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sns_project.Adapter.ShowPostImageAdapter;
import com.example.sns_project.Info.PostInfo;
import com.example.sns_project.databinding.ActivityPostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

//이 곳에서 작성한 글과 파일을 볼 수 있으며 댓글과 좋아요 버튼을 누를 수 있다.
//        작성한 글 db에서 내용을 가져옴으로써 구현하고,
//        댓글과 좋아요 버튼을 누르면 해당 게시글db에 내용이 추가로 입력되도록한다.

public class PostActivity extends AppCompatActivity {
    ActivityPostBinding binding;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        PostInfo postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
        setPost(postInfo);

    }

    private void setPost(PostInfo postInfo) {

        binding.nicknamePostT.setText(postInfo.getPublisher());
        binding.datePostT.setText(new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault()).format(postInfo.getCreatedAt()).toString());
        binding.titlePostT.setText(postInfo.getTitle());
        binding.contentPostT.setText(postInfo.getContents());
        binding.goodNumPostT.setText(postInfo.getGood() + "");
        binding.commentNumPostT.setText(postInfo.getComment() + "");

        if (postInfo.getFormats() != null) {
            Log.d("겟포멧 널아님 입성","입성");
            binding.formatsLinearLayout.setVisibility(View.VISIBLE);
            Add_and_SetRecyclerView(PostActivity.this,postInfo.getFormats());
        }

    }

    public void Add_and_SetRecyclerView(Activity activity, ArrayList<String> formats){

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.formatsRecycler.setLayoutManager(layoutManager);

        ShowPostImageAdapter showPostImageAdapter = new ShowPostImageAdapter(activity,formats);
        binding.formatsRecycler.setAdapter(showPostImageAdapter);

    }

}