package com.example.sns_project.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import com.example.sns_project.util.Named;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.example.sns_project.util.Named.DeleteResult;
import static com.example.sns_project.util.Named.GoodResult;
import static com.example.sns_project.util.Named.WriteResult;

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
    private Named named = new Named();
    private boolean GOOD_ACTION = false;

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
        setPost();
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
            case android.R.id.home://////////////////////////////////////////////////////////////////////////////////////백버튼 기능추가 요망
                //select back button
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("SetTextI18n")
    private void setPost() {

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
    public void good_up_btn(View view){ //좋아요 버튼 누르면 db의 해당 게시물의 좋아요수가 증가한다.

       //postinfo로 해당게시물에 좋아요를 누른 사람 id를 저장해주고,
        //좋아요 누른 사람이 중복으로 누르지않게 id를 찾아서 있으면 아닌거고 없으면 좋아요+1
        if(postInfo.getId().equals(user.getUid())){
            Toast("자신의 게시물에는 좋아요를 누를 수 없습니다.");
            return;
        }

        DocumentReference docref = db.collection(postInfo.getLocation()).document(postInfo.getDocid());

        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        HashMap<String,Integer> good_users = (HashMap<String,Integer>) document.getData().get("good_user");
                        if(good_users.containsKey(user.getUid())) //중복으로 누른다면
                        {
                            Toast("이미 눌렀어요!");
                        }else{ //처음 누른다면
                            good_users.put(user.getUid(),1);
                            docref.update("good_user",good_users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    docref.update("good",good_users.size()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        //좋아요가 db에 올라간 이후
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            GOOD_ACTION = true;
                                            ////////////////////////////////////////////////////////////////////////////////////////////////게시글에서 바로 좋아요가 갱신되는 걸 나타내야함.
                                            docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                //다시 게시물을 가져와서 굳이?라고 할법하지만 그냥 +1이 아니라 동시사용자가 있기때문에 현재상황 반영
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    DocumentSnapshot document = task.getResult();
                                                    binding.goodNumPostT.setText(document.get("good").toString());
                                                    Toast("좋아요!");
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            }

        });
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
                        toMain(DeleteResult,postInfo.getDocid());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast("삭제 실패하였습니다. :db");
                    }
                });
    }

    public void toMain(int result,String docid){ //게시물번호를 넘겨주고 frag에서 처리하기위함.
        Intent intent = new Intent();
        intent.putExtra("docid",docid);
        setResult(WriteResult,intent);
        finish();
    }

    @Override
    public void onBackPressed() {

        if (GOOD_ACTION) { //좋아요 버튼 눌렀으면 리스트 리셋
            toMain(GoodResult,postInfo.getDocid());
        }else{
            finish();
        }

    }

    public void Toast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
}