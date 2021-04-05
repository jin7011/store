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
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.sns_project.Adapter.CommentsAdapter;
import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.Adapter.ShowPostImageAdapter;
import com.example.sns_project.R;
import com.example.sns_project.databinding.ActivityPostBinding;
import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.info.RecommentInfo;
import com.example.sns_project.util.Named;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.example.sns_project.util.Named.DeleteResult;
import static com.example.sns_project.util.Named.PostAddComment;
import static com.example.sns_project.util.Named.PostInitComment;
import static com.example.sns_project.util.Named.Something_IN_Post;
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
    private boolean COMMENT_ACTION = false;
    private CommentsAdapter commentsAdapter;
    private ArrayList<CommentInfo> comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Bundle bundle = getIntent().getExtras();
        postInfo = bundle.getParcelable("postInfo");

//        postInfo = (PostInfo) getIntent().getParcelableExtra("postInfo"); //홀더에서 받아온 게시물정보

        Log.d("포스트액티","getComments: "+postInfo.getComments());
        Log.d("포스트액티","getCreatedAt: "+postInfo.getCreatedAt());
        Log.d("포스트액티","getDocid: "+postInfo.getDocid());
        Log.d("포스트액티","getGood_user: "+postInfo.getGood_user());
        Log.d("포스트액티","getGood: "+postInfo.getGood());
        Log.d("포스트액티","getComment: "+postInfo.getComment());
        Log.d("포스트액티","getFormats: "+postInfo.getFormats());
        Log.d("포스트액티","getId: "+postInfo.getId());
        Log.d("포스트액티","getContents: "+postInfo.getContents());
        Log.d("포스트액티","getLocation: "+postInfo.getLocation());
        Log.d("포스트액티","getPublisher: "+postInfo.getPublisher());
        Log.d("포스트액티","getTitle: "+postInfo.getTitle());
        Log.d("포스트액티","getStoragePath: "+postInfo.getStoragePath());
        setPost();
        setToolbar();

        binding.AddCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_comment();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setPost() {

        binding.nicknamePostT.setText(postInfo.getPublisher());
        binding.datePostT.setText(new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault()).format(postInfo.getCreatedAt()));
        binding.titlePostT.setText(postInfo.getTitle());
        binding.contentPostT.setText(postInfo.getContents());
        binding.goodNumPostT.setText(postInfo.getGood() + "");
        binding.commentNumPostT.setText(postInfo.getComment() + "");
        comments = postInfo.getComments();
        Check_Comment(PostInitComment);
        //todo 댓글추가해야함  (받아온 정보에 댓글이 있는지 확인하고 있으면 visible 해줘야함 그리고 리사이클러뷰 세팅해줘야댐 없으면 gone처리해줘야 재활용안댐)

        if (postInfo.getFormats() != null && postInfo.getFormats().size() != 0) {
            Log.d("겟포멧 널아님 입성","입성");
            binding.formatsLinearLayout.setVisibility(View.VISIBLE);
            Add_and_Set_ImageRecyclerView(PostActivity.this,postInfo.getFormats());
        }

    }

    private void add_comment(){
        String comment = binding.AddCommentT.getText().toString();
        CommentInfo commentInfo = new CommentInfo(comment,user.getDisplayName(),new Date(),user.getUid(),0);
        DocumentReference docref = db.collection(postInfo.getLocation()).document(postInfo.getDocid());

        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<CommentInfo> commentInfoArrayList = get_commentArray_from_Firestore(document);
                        commentInfoArrayList.add(commentInfo);
                        docref.update("comments", commentInfoArrayList).addOnCompleteListener(new OnCompleteListener<Void>() { //댓글 db에 올리고,
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                docref.update("comment",commentInfoArrayList.size()).addOnCompleteListener(new OnCompleteListener<Void>() { //댓글 갯수 +1 해주고,
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Check_Comment(PostAddComment);
                                        commentsAdapter.CommentInfo_DiffUtil(commentInfoArrayList);
                                        comments.clear();
                                        comments.addAll(commentInfoArrayList);
                                        binding.commentNumPostT.setText(commentInfoArrayList.size()+"");
                                        hideKeyPad();
                                    }
                                });
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast("삭제된 게시물입니다.");
            }
        });
    }

    public void Check_Comment(int order){
        Log.d("zzxzxzx:    ",comments.size()+"");

        if(order == PostInitComment) {
            if (comments.size() != 0) { //댓글이 있는 게시물은 갱신
                Log.d("zzxzxzx:    ","init: "+PostInitComment+"");
                binding.commentLinearLayout.setVisibility(View.VISIBLE);
                Add_and_Set_CommentRecyclerView(PostActivity.this);
                commentsAdapter.CommentInfo_DiffUtil(comments);
            } else { //아님 닫음
                binding.commentLinearLayout.setVisibility(View.GONE);
            }
        }

        if(order == PostAddComment){
            COMMENT_ACTION = true;
            if (comments.size() == 0) {
                binding.commentLinearLayout.setVisibility(View.VISIBLE);
                Add_and_Set_CommentRecyclerView(PostActivity.this);
            }
        }
    }

    public void Add_and_Set_CommentRecyclerView(Activity activity){
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setItemPrefetchEnabled(true); //렌더링 퍼포먼스 향상
        binding.commentRecycler.setLayoutManager(layoutManager);
        commentsAdapter = new CommentsAdapter(activity);
        binding.commentRecycler.setAdapter(commentsAdapter);

        RecyclerView.ItemAnimator animator = binding.commentRecycler.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    public void Add_and_Set_ImageRecyclerView(Activity activity, ArrayList<String> formats){

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.formatsRecycler.setLayoutManager(layoutManager);

        ShowPostImageAdapter showPostImageAdapter = new ShowPostImageAdapter(activity,formats);
        binding.formatsRecycler.setAdapter(showPostImageAdapter);

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


    //todo 아예 포스트의 좋아요와 댓글까지 싹 갱신하는 함수를 만들자. 그래서 좋아요를 누르거나 댓글을 달면 바로 갱신될 수 있도록 해주자
    @SuppressLint("SetTextI18n")
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
                            //이곳에서 미리 텍스트처리
                            GOOD_ACTION = true;
                            String good = binding.goodNumPostT.getText().toString();
                            binding.goodNumPostT.setText( ( Integer.parseInt(good)+1 )+"" );
                            Toast("좋아요!");

                            //이후에 백그라운드로 DB처리
                            good_users.put(user.getUid(),1);
                            docref.update("good_user",good_users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    docref.update("good",good_users.size());
                                }
                            });
                        }
                    }
                }
            }

        });
    }

    //todo 좋아요 갱신하는 함수 따로만들어서 편하게쓰자

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
        setResult(result,intent);
        finish();
    }

    public ArrayList<CommentInfo> get_commentArray_from_Firestore(DocumentSnapshot document){

        ArrayList<CommentInfo> commentInfoArrayList = new ArrayList<>();

        if(((ArrayList<HashMap<String,Object>>) document.getData().get("comments")).size() != 0){
            for(int x=0; x<((ArrayList<HashMap<String,Object>>) document.getData().get("comments")).size(); x++) {
                HashMap<String, Object> map = ((ArrayList<HashMap<String, Object>>) document.getData().get("comments")).get(x);

                CommentInfo commentInfo = new CommentInfo((String) map.get("contents"), (String) map.get("publisher"),
                        ((Timestamp)map.get("createdAt")).toDate(),
                        (String) map.get("id"),
                        ((Long)(map.get("good"))).intValue());

                commentInfoArrayList.add(commentInfo);
            }
        }

        return commentInfoArrayList;
    }

    @Override
    public void onBackPressed() {

        if (GOOD_ACTION || COMMENT_ACTION) { //좋아요 버튼 눌렀으면 리스트 리셋
            toMain(Something_IN_Post,postInfo.getDocid());
        }else{
            finish();
        }

    }

    public void hideKeyPad(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.AddCommentT.getWindowToken(), 0);
        binding.AddCommentT.setText(null);
    }

    public void Toast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
}