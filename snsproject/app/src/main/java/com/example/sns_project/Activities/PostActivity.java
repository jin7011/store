package com.example.sns_project.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sns_project.Adapter.ShowPostImageAdapter;
import com.example.sns_project.R;
import com.example.sns_project.databinding.ActivityPostBinding;
import com.example.sns_project.info.PostInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private PostInfo postInfo;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Toolbar toolbar;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
        setPost(postInfo);
        setToolbar();
    }

    public void setToolbar(){
        toolbar = findViewById(R.id.toolbar_post);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다.
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.d("asd","post: "+postInfo.getId()+"나 : "+ user.getUid());

        if(postInfo.getId().equals(user.getUid())) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.mypost_menu, menu);
            return super.onCreateOptionsMenu(menu);
        }
        else{
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.otherspost_menu, menu);
            return super.onCreateOptionsMenu(menu);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:
                PostDelete(0);
                break;
            case R.id.scrap:
                break;
            case R.id.submission:
                break;
            case android.R.id.home:
                //select back button
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    private void PostDelete(int delcnt){

        if(postInfo.getFormats() != null) {
            final StorageReference fileRef = storageRef.child(postInfo.getStoragePath().get(delcnt));

            fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    if (delcnt < postInfo.getStoragePath().size() - 1) {
                        PostDelete(delcnt + 1);

                    } else if (delcnt == postInfo.getStoragePath().size() - 1) {
                        DB_del();
                        return;
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d("asdasd", postInfo.getLocation() + "/" + postInfo.getDocid());
                    Toast("삭제 실패하였습니다. :storage" + postInfo.getLocation() + "/" + postInfo.getDocid());
                }
            });
        }else {
            DB_del();
        }

    }

    public void DB_del(){
        db.collection(postInfo.getLocation()).document(postInfo.getDocid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast("삭제되었습니다.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast("삭제 실패하였습니다. :db");
                    }
                });
    }

    public void Toast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
}