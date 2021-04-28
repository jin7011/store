package com.example.sns_project.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.sns_project.Adapter.CommentsAdapter;
import com.example.sns_project.Adapter.ShowPostImageAdapter;
import com.example.sns_project.Listener.Listener_CommentHolder;
import com.example.sns_project.Listener.Listener_PostImageHolder;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_PostInfo;
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
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.example.sns_project.util.Named.DeleteResult;
import static com.example.sns_project.util.Named.Something_IN_Post;

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
//    private boolean GOOD_ACTION = false;
//    private boolean COMMENT_ACTION = false;
    private boolean ACTION = false;
    private CommentsAdapter commentsAdapter;
    private ArrayList<CommentInfo> comments;
    private LiveData_PostInfo liveData_postInfo;
    private CommentsAdapter.CommentsHolder PostcommentsHolder;

    //todo 추가적으로 하는 일(댓글,좋아요,글쓰기)에 대해서 동시적인 작업처리를 해줘야할 때가 왔음 (아마도 트랜젝션이 제일 유일)
    @Override
    protected void onCreate(Bundle savedInstanceState) { //todo 게시물 내부에 새로고침 만들
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        liveData_postInfo = new ViewModelProvider(this).get(LiveData_PostInfo.class);
        liveData_postInfo.get().observe(this, new Observer<PostInfo>() { // 전반적인 게시물의 내용
            @Override
            public void onChanged(PostInfo postInfo) {
                commentsAdapter.CommentInfo_DiffUtil(postInfo.getComments());
                Log.d("포스트액티zx","getComment: "+postInfo.getComment());
                binding.setPostInfo(postInfo);
                ACTION = true;
            }
        });

        Bundle bundle = getIntent().getExtras();
        postInfo = bundle.getParcelable("postInfo");
        liveData_postInfo.get().setValue(postInfo);

        Log.d("포스트액티","getComments: "+postInfo.getComments());
        Log.d("포스트액티","getCreatedAt: "+postInfo.getDateFormate_for_layout());
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
        Add_and_Set_CommentRecyclerView(this);

        binding.AddCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.AddCommentT.getText() != null && binding.AddCommentT.getText().toString().length() > 0) {
                    if(PostcommentsHolder == null)
                        add_comment();
                    else
                        add_recomment();
                }
                else{
                    Toast("글자를 입력해주세요.");
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setPost() {

        if (postInfo.getFormats() != null && postInfo.getFormats().size() != 0) {
            Log.d("겟포멧 널아님 입성","입성");
            binding.formatsLinearLayout.setVisibility(View.VISIBLE);
            Add_and_Set_ImageRecyclerView(PostActivity.this,postInfo.getFormats());
        }

    }
    private void add_recomment(){
        //todo transaction
        if(PostcommentsHolder == null)
            return;

        RelativeLayout loader = findViewById(R.id.loaderLyaout);
        loader.setVisibility(View.VISIBLE); //로딩화면
        hideKeyPad(); //보기안좋으니까 키패드 내리고

        String comment = binding.AddCommentT.getText().toString();
        RecommentInfo recommentInfo = new RecommentInfo(comment,user.getDisplayName(),new Date(),user.getUid(),0);
        DocumentReference docref = db.collection(postInfo.getLocation()).document(postInfo.getDocid());

        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<CommentInfo> commentInfoArrayList = get_commentArray_from_Firestore(document);
                        PostInfo newpostInfo = liveData_postInfo.get().getValue();
                        int commentnum =  ((Long)document.get("comment")).intValue();

                        for(int x=0; x<commentInfoArrayList.size(); x++){
                            String db_comment_key = commentInfoArrayList.get(x).getKey();
                            String holder_comment_key = postInfo.getComments().get(PostcommentsHolder.getAbsoluteAdapterPosition()).getKey();

                            if(db_comment_key.equals(holder_comment_key)){ //db에서 대댓글을 달려고하는 해당 댓글을 key값으로 찾았다면,
                                commentInfoArrayList.get(x).getRecomments().add(recommentInfo);
                                Set_CommentDB(commentInfoArrayList,newpostInfo,commentnum,loader,docref);
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast("삭제된 게시물/댓글입니다.");
            }
        });
    }

    private void add_comment(){
        //todo transaction
        RelativeLayout loader = findViewById(R.id.loaderLyaout);
        loader.setVisibility(View.VISIBLE); //로딩화면
        hideKeyPad(); //보기안좋으니까 키패드 내리고

        String comment = binding.AddCommentT.getText().toString();
        Date date = new Date();

        CommentInfo commentInfo = new CommentInfo(comment,user.getDisplayName(),date,user.getUid(),0,new ArrayList<>(),
                postInfo.getDocid()+date.getTime());
        DocumentReference docref = db.collection(postInfo.getLocation()).document(postInfo.getDocid());

        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        int commentnum =  ((Long)document.get("comment")).intValue();
                        ArrayList<CommentInfo> commentInfoArrayList = get_commentArray_from_Firestore(document);
                        PostInfo newpostInfo = liveData_postInfo.get().getValue(); //얕은 복사라 사실상 다같이 지워지고 다같이 리셋되지만, 걍 쓰기엔 변수명이 너무 길어서 새로 팠음
                        commentInfoArrayList.add(commentInfo);

                        Set_CommentDB(commentInfoArrayList,newpostInfo,commentnum,loader,docref);

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

    public void Set_CommentDB(ArrayList<CommentInfo> commentInfoArrayList,PostInfo newpostInfo,int commentnum,RelativeLayout loader, DocumentReference docref){

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(docref);

                //error 트랜잭션으로 넘겨주지만 1초 이내의 동시작업은 에러를 야기하는 치명적인 단점이 존재한다. (거의 동시에 두개 이상의 댓글이 올라가면 하나만 적용되는 에러 -> 하지만 둘다 success로 표기됨)
                transaction.update(docref, "comment", commentnum+1);
                transaction.update(docref, "comments", commentInfoArrayList);

                // Success
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                newpostInfo.setComment(commentnum+1); //댓글수 +1
                newpostInfo.getComments().clear();
                newpostInfo.getComments().addAll(commentInfoArrayList);
                liveData_postInfo.get().setValue(newpostInfo);

                binding.commentNumPostT.setText(commentInfoArrayList.size()+"");
                loader.setVisibility(View.GONE);
                binding.AddCommentT.setText(null);

                if(PostcommentsHolder != null){
                    commentsAdapter.Off_CommentbodyColor(PostcommentsHolder);
                    PostcommentsHolder = null;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loader.setVisibility(View.GONE);
                Toast("댓글에 실패했습니다.");
            }
        });

    }

    public void Add_and_Set_CommentRecyclerView(PostActivity activity){

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setItemPrefetchEnabled(true); //렌더링 퍼포먼스 향상

        binding.commentRecycler.setLayoutManager(layoutManager);
        commentsAdapter = new CommentsAdapter(activity, postInfo, new Listener_CommentHolder() {
            @Override
            public void onClickedholder(CommentsAdapter.CommentsHolder commentsHolder) {
                CommentInfo commentInfo = postInfo.getComments().get(commentsHolder.getAbsoluteAdapterPosition());
                if(PostcommentsHolder == null)
                    PostcommentsHolder = commentsHolder;
                else{
                    commentsAdapter.Off_CommentbodyColor(PostcommentsHolder);
                    PostcommentsHolder = commentsHolder;
                }
                Toast(""+commentInfo.getContents());
            }
        });
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

        ShowPostImageAdapter showPostImageAdapter = new ShowPostImageAdapter(activity, formats, new Listener_PostImageHolder() {
            @Override
            public void onClickedholder(ShowPostImageAdapter.ShowPostImageHolder showPostImageHolder) {
                //todo 이미지 클릭작업 (해당 이미지의 홀더를 리스너로 받아왔음.)
                Toast("position: "+showPostImageHolder.getAbsoluteAdapterPosition());
                Intent intent = new Intent(PostActivity.this,View_FormatActivity.class);
                intent.putExtra("position",showPostImageHolder.getAbsoluteAdapterPosition());
                intent.putExtra("formats",postInfo.getFormats());
                PostActivity.this.startActivity(intent);
            }
        });
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

    //todo 아예 포스트의 좋아요와 댓글까지 싹 갱신하는 함수를 만들자. 그래서 좋아요를 누르거나 댓글을 달면 바로 갱신될 수 있도록 해주자  ㅊ
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
                            //이후에 백그라운드로 DB처리
                            db.runTransaction(new Transaction.Function<Void>() {
                                @Override
                                public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentSnapshot snapshot = transaction.get(docref);

                                    //이후에 백그라운드로 DB처리
                                    good_users.put(user.getUid(),1);
                                    Long newPopulation = snapshot.getLong("good") + 1;
                                    transaction.update(docref, "good", newPopulation.intValue());
                                    transaction.update(docref, "good_user", good_users);

                                    // Success
                                    return null;
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("트랜잭션좋아요", "Transaction success!");
                                    String good = binding.goodNumPostT.getText().toString();
                                    binding.goodNumPostT.setText( ( Integer.parseInt(good)+1 )+"");
                                    Toast("좋아요!");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("트랜잭션좋아요", "Transaction failure.", e);
                                }
                            });
                        }
                    }
                }
            }

        });
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
                    Log.d("storage_Delete", postInfo.getLocation() + "/" + postInfo.getDocid());
                    Toast("삭제 실패하였습니다. :storage" + postInfo.getLocation() + "/" + postInfo.getDocid());
                }
            });
        }else {
            DB_del();
        }

    }

    public void Reset(){

        db.collection(postInfo.getLocation()).document(postInfo.getDocid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                DocumentSnapshot document = task.getResult();
                ArrayList<CommentInfo> commentInfoArrayList = get_commentArray_from_Firestore(document);

                PostInfo newpostInfo = new PostInfo(
                        document.get("id").toString(),
                        document.get("publisher").toString(),
                        document.get("title").toString(),
                        document.get("contents").toString(),
                        (ArrayList<String>) document.getData().get("formats"),
                        new Date(document.getDate("createdAt").getTime()),
                        document.getId(),
                        Integer.parseInt(document.get("good").toString()), Integer.parseInt(document.get("comment").toString()), document.get("location").toString(),
                        (ArrayList<String>) document.getData().get("storagepath"),commentInfoArrayList,
                        (HashMap<String, Integer>)document.getData().get("good_user")
                );
                liveData_postInfo.get().setValue(newpostInfo);
                Toast("새로고침 되었습니다.");
            }
        });

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
                HashMap<String, Object> commentsmap = ((ArrayList<HashMap<String, Object>>) document.getData().get("comments")).get(x);

                CommentInfo commentInfo = new CommentInfo((String) commentsmap.get("contents"), (String) commentsmap.get("publisher"),
                        ((Timestamp)commentsmap.get("createdAt")).toDate(),
                        (String) commentsmap.get("id"),
                        ((Long)(commentsmap.get("good"))).intValue(),
                        get_RecommentArray_from_commentsmap(commentsmap),
                        (String) commentsmap.get("key")
                );

                commentInfoArrayList.add(commentInfo);
            }
        }

        return commentInfoArrayList;
    }

    public ArrayList<RecommentInfo> get_RecommentArray_from_commentsmap( HashMap<String, Object> commentsmap ){
        ArrayList<RecommentInfo> recommentInfoArrayList = new ArrayList<>();
        ArrayList<HashMap<String, Object>> recomments = (ArrayList<HashMap<String, Object>>)commentsmap.get("recomments");

        for(int x=0; x<recomments.size(); x++) {
            HashMap<String, Object> recommentsmap = recomments.get(x);
            RecommentInfo recommentInfo = new RecommentInfo(
                    (String)recommentsmap.get("contents"),
                    (String)recommentsmap.get("publisher"),
                    ((Timestamp)recommentsmap.get("createdAt")).toDate(),
                    (String)recommentsmap.get("id"),
                    ((Long)(recommentsmap.get("good"))).intValue()
                    );

            recommentInfoArrayList.add(recommentInfo);
        }

        return recommentInfoArrayList;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:
                PostDelete(0);
                break;
            case R.id.scrap:
                Toast("스크랩되었습니다.");
                break;
            case R.id.submission:
                Toast("신고되었습니다.");
                break;
            case R.id.autonew:

                item.setEnabled(false);
                Reset();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        item.setEnabled(true);
                    }
                }, 1500); //딜레이 타임 조절
                break;
            case android.R.id.home:
                //select back button
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if(PostcommentsHolder != null){
            commentsAdapter.Off_CommentbodyColor(PostcommentsHolder);
            PostcommentsHolder = null;
        }else {
            if (ACTION) { //좋아요 버튼 눌렀으면 리스트 리셋
                toMain(Something_IN_Post, postInfo.getDocid());
            } else {
                finish();
            }
        }
    }

    public void hideKeyPad(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.AddCommentT.getWindowToken(), 0);
    }

    public void ShowKeyPad(){
        binding.AddCommentT.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void Toast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

}