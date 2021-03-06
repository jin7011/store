package com.example.sns_project.activity;

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
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.sns_project.Adapter.CommentsAdapter;
import com.example.sns_project.Adapter.ShowPostImageAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.Listener.Listener_PostImageHolder;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_PostInfo;
import com.example.sns_project.databinding.ActivityPostBinding;
import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.Declaration;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.info.NotificationInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.info.RecommentInfo;
import com.example.sns_project.util.My_Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.example.sns_project.util.Named.DELETE_RESULT;
import static com.example.sns_project.util.Named.HORIZEN;
import static com.example.sns_project.util.Named.SOMETHING_IN_POST;
import static com.example.sns_project.util.Named.VERTICAL;

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
    private My_Utility my_utility;
    private boolean ACTION = false;
    private CommentsAdapter commentsAdapter;
    private LiveData_PostInfo liveData_postInfo;
    private CommentsAdapter.CommentsHolder PostcommentsHolder;
    private PostControler postControler;
    private RelativeLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        loader = findViewById(R.id.loaderLyaout);

        liveData_postInfo = new ViewModelProvider(this).get(LiveData_PostInfo.class);
        liveData_postInfo.get().observe(this, new Observer<PostInfo>() { // 전반적인 게시물의 내용
            @Override
            public void onChanged(PostInfo NewPostInfo) {
                commentsAdapter.CommentInfo_DiffUtil(NewPostInfo); // 댓글어댑터 내부에서 postinfo를 최신으로 받아서 처리해야 db사용이 원할해지므로 포스트 자체를 넘겨주었다. (좋아요,댓글 수 등등 때문에)
                binding.setPostInfo(NewPostInfo);
                postInfo = new PostInfo(NewPostInfo);
                Log.d("포스트액티zx", "post: " + postInfo.hashCode() + ", " + NewPostInfo.hashCode());
                ACTION = true;
            }
        });

        Bundle bundle = getIntent().getExtras();
        postInfo = bundle.getParcelable("postInfo");
        liveData_postInfo.get().setValue(new PostInfo(postInfo)); //처음 들어왔을 때 셋팅

        Log.d("포스트액티", "getComments: " + postInfo.getComments());
        Log.d("포스트액티", "getCreatedAt: " + postInfo.getDateFormate_for_layout());
        Log.d("포스트액티", "getDocid: " + postInfo.getDocid());
        Log.d("포스트액티", "getGood_user: " + postInfo.getGood_user());
        Log.d("포스트액티", "getGood: " + postInfo.getGood());
        Log.d("포스트액티", "getComment: " + postInfo.getComment());
        Log.d("포스트액티", "getFormats: " + postInfo.getFormats());
        Log.d("포스트액티", "getId: " + postInfo.getId());
        Log.d("포스트액티", "getContents: " + postInfo.getContents());
        Log.d("포스트액티", "getLocation: " + postInfo.getLocation());
        Log.d("포스트액티", "getPublisher: " + postInfo.getPublisher());
        Log.d("포스트액티", "getTitle: " + postInfo.getTitle());
        Log.d("포스트액티", "getStoragePath: " + postInfo.getStoragePath());

        Log.d("포스트어댑터", "comment_good_user: " + postInfo.getComments().size());
        for (int x = 0; x < postInfo.getComments().size(); x++) {
            Log.d("포스트어댑터", "comment_good_user: " + x + ": ,.,." + postInfo.getComments().get(x).getGood_user());
        }

        setPost();
        setToolbar();
        Add_and_Set_CommentRecyclerView(this);

        binding.AddCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.AddCommentT.getText() != null && binding.AddCommentT.getText().toString().length() > 0) {
                    if (PostcommentsHolder == null)
                        add_comment();
                    else
                        add_recomment();
                } else {
                    Toast("글자를 입력해주세요.");
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setPost() {
        if (postInfo.getFormats() != null && postInfo.getFormats().size() != 0) {
            Log.d("겟포멧 널아님 입성", "입성");
            binding.formatsLinearLayout.setVisibility(View.VISIBLE);
            Add_and_Set_ImageRecyclerView(PostActivity.this, postInfo.getFormats());
        }
    }

    private void add_recomment() {
        if (PostcommentsHolder == null)
            return;

        Loading(true);

        CommentInfo commentInfo = postInfo.getComments().get(PostcommentsHolder.getAbsoluteAdapterPosition());
        String key = commentInfo.getKey();
        String comment = binding.AddCommentT.getText().toString();
        RecommentInfo NewRecomment = new RecommentInfo(comment, user.getDisplayName(), new Date(), user.getUid(), 0, new HashMap<String, Integer>());

        postControler.Update_ReComments_With_Transaction(postInfo.getDocid(), key, NewRecomment, new PostControler.Listener_Complete_Set_PostInfo_Transaction() {
            @Override
            public void onComplete_Set_PostInfo(PostInfo NewPostInfo) {

                if (NewPostInfo != null) //해당 댓글이 존재하고 그곳에 대댓글을 다는 것에 문제가 없다면 null이 아니다.
                {
                    liveData_postInfo.get().setValue(NewPostInfo); //최신 게시판 상태를 모델에 셋시켜줌.

                    if(!user.getUid().equals(commentInfo.getId())) {//내가 댓글단게 아니라면 해당 댓글에 노티를 준다.
                        FirebaseFirestore.getInstance().collection("USER").document(commentInfo.getId())
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                MyAccount account = task.getResult().toObject(MyAccount.class);
                                NotificationInfo noti = new NotificationInfo("Recomment", account.getToken(), user.getDisplayName(), comment,
                                        commentInfo.getId(), new Date().getTime());
                                FirebaseFirestore.getInstance().collection("USER").document(commentInfo.getId()).collection("Notification")
                                        .document(commentInfo.getId()).set(noti);
                                Loading(false);
                            }
                        });
                    }else{
                        Loading(false);
                    }
                }
                else {
                    Toast("삭제된 게시물/댓글입니다.");
                    Loading(false);
                    finish();
                }

                Loading(false);
            }

            @Override
            public void onFailed() {
                Loading(false);
                Toast("삭제된 게시물/댓글입니다.");
                finish();
            }
        });
    }

    private void add_comment() {
        Loading(true);

        final String[] comment = {binding.AddCommentT.getText().toString()};
        Date date = new Date();

        CommentInfo commentInfo = new CommentInfo(comment[0], user.getDisplayName(), date, user.getUid(), 0, new HashMap<String, Integer>(), new ArrayList<>(),
                postInfo.getDocid() + date.getTime());

        postControler.Update_Comments_With_Transaction(postInfo.getDocid(), commentInfo, new PostControler.Listener_Complete_Set_PostInfo_Transaction() {
            @Override
            public void onComplete_Set_PostInfo(PostInfo NewPostInfo) {
                if(NewPostInfo != null) {
                    liveData_postInfo.get().setValue(NewPostInfo); //최신 게시판 상태를 모델에 셋시켜줌.

                    if(!user.getUid().equals(postInfo.getId())) { //내 글에 내가 댓글 다는 경우가 아니라면 노티를 준다.
                        FirebaseFirestore.getInstance().collection("USER").document(postInfo.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                MyAccount account = task.getResult().toObject(MyAccount.class);
                                NotificationInfo noti = new NotificationInfo("Comment", account.getToken(), postInfo.getTitle(),
                                        comment[0], postInfo.getDocid(), new Date().getTime());
                                FirebaseFirestore.getInstance().collection("USER").document(postInfo.getId()).collection("Notification").document(postInfo.getDocid()).set(noti);
                                Loading(false);
                            }
                        });
                    }else{
                        Loading(false);
                    }
                }else{
                    Loading(false);
                    Toast("삭제된 게시물/댓글입니다.");
                    finish();
                }
            }

            @Override
            public void onFailed() {
                Loading(false);
                Toast("댓글에 실패했습니다.");
            }
        });
    }

        public void Add_and_Set_CommentRecyclerView (PostActivity activity){

            commentsAdapter = new CommentsAdapter(activity, postInfo,
                    new CommentsAdapter.Listener_CommentHolder() { //대댓글을 위해서 확인을 누르고 대댓글을 달려는 순간 나오는 리스너

                        @Override
                        public void onClickedholder(CommentsAdapter.CommentsHolder commentsHolder) {
                            CommentInfo commentInfo = postInfo.getComments().get(commentsHolder.getAbsoluteAdapterPosition());
                            if (PostcommentsHolder == null)
                                PostcommentsHolder = commentsHolder;
                            else {
                                commentsAdapter.Off_CommentbodyColor(PostcommentsHolder);
                                PostcommentsHolder = commentsHolder;
                            }
                            Toast("" + commentInfo.getContents());
                        }
                    }, new CommentsAdapter.Listener_Pressed_goodbtn() { //댓글의 좋아요 버튼을 눌러서 나온 리스너

                @Override
                public void onClicked_goodbtn(PostInfo NewPostInfo) { //좋아요 버튼
                    liveData_postInfo.get().setValue(NewPostInfo);
                }
            }, new CommentsAdapter.Listener_Comment_Delete() {

                @Override
                public void onClick_Delete_Comment(CommentInfo commentInfo) { //댓글삭제
                    loader.setVisibility(View.VISIBLE);
                    postControler.Delete_Comment(postInfo, commentInfo, new PostControler.Listener_Delete_Comment() {
                        @Override
                        public void onComplete(PostInfo postInfo) {
                            liveData_postInfo.get().setValue(postInfo);
                            loader.setVisibility(View.GONE);
                        }
                        @Override
                        public void onFail() {
                            Toast("삭제에 실패하였습니다.");
                            loader.setVisibility(View.GONE);
                        }
                    });
                }
                @Override
                public void onClick_Delete_Recomment(RecommentInfo recomment,CommentInfo comment) { //대댓글삭제
                    loader.setVisibility(View.VISIBLE);
                    postControler.Delete_Recomment(postInfo, comment, recomment, new PostControler.Listener_Delete_Comment() {
                        @Override
                        public void onComplete(PostInfo postInfo) {
                            liveData_postInfo.get().setValue(postInfo);
                            loader.setVisibility(View.GONE);
                        }
                        @Override
                        public void onFail() {
                            Toast("삭제에 실패하였습니다.");
                            loader.setVisibility(View.GONE);
                        }
                    });
                }
            });

            my_utility = new My_Utility(this, binding.commentRecycler, commentsAdapter);
            my_utility.RecyclerInit(VERTICAL);
            postControler = new PostControler(postInfo.getLocation(), my_utility);

        }

        public void Add_and_Set_ImageRecyclerView (Activity activity, ArrayList < String > formats){

            ShowPostImageAdapter showPostImageAdapter = new ShowPostImageAdapter(activity, formats, new Listener_PostImageHolder() {
                @Override
                public void onClickedholder(ShowPostImageAdapter.ShowPostImageHolder showPostImageHolder) {
//                Toast("position: "+showPostImageHolder.getAbsoluteAdapterPosition());
                    Intent intent = new Intent(PostActivity.this, View_FormatActivity.class);
                    intent.putExtra("position", showPostImageHolder.getAbsoluteAdapterPosition());
                    intent.putExtra("formats", postInfo.getFormats());
                    PostActivity.this.startActivity(intent);
                }
            });
            my_utility = new My_Utility(this, binding.formatsRecycler, showPostImageAdapter);
            my_utility.RecyclerInit(HORIZEN);
        }

        public void setToolbar () {
            toolbar = findViewById(R.id.toolbar_post);
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){

            Log.d("asd", "post: " + postInfo.getId() + "나 : " + user.getUid());

            if (postInfo.getId().equals(user.getUid())) {
                MenuInflater menuInflater = getMenuInflater();
                menuInflater.inflate(R.menu.mypost_menu, menu);
                return super.onCreateOptionsMenu(menu);
            } else {
                MenuInflater menuInflater = getMenuInflater();
                menuInflater.inflate(R.menu.otherspost_menu, menu);
                return super.onCreateOptionsMenu(menu);
            }
        }

        @SuppressLint("SetTextI18n")
        public void good_up_btn (View view){ //좋아요 버튼 누르면 db의 해당 게시물의 좋아요수가 증가한다.

            //postinfo로 해당게시물에 좋아요를 누른 사람 id를 저장해주고,
            //좋아요 누른 사람이 중복으로 누르지않게 id를 찾아서 있으면 아닌거고 없으면 좋아요+1
            postControler.Press_Good_Post(postInfo, new PostControler.Listener_Complete_GoodPress() {
                @Override
                public void onComplete_Good_Press(PostInfo NewPostInfo) {
                    liveData_postInfo.get().setValue(NewPostInfo);
                    Toast("좋아요!");
                }

                @Override
                public void onFailed() {
                    Toast("존재하지 않는 게시물/댓글입니다.");
                }

                @Override
                public void AlreadyDone() {
                    Toast("이미 눌렀어요!");
                }

                @Override
                public void CannotSelf() {
                    Toast("자신의 게시물에는 '좋아요'를 누를 수 없습니다.");
                }
            });

        }

        public void Reset () {
            postControler.Get_UniPost(postInfo.getDocid(), new PostControler.Listener_Complete_Get_PostInfo() {
                @Override
                public void onComplete_Get_PostInfo(PostInfo postInfo) {
                    liveData_postInfo.get().setValue(postInfo);
                    Toast("새로고침 되었습니다.");
                }
            });
        }

        private void PostDelete ( int delcnt){

            if (postInfo.getFormats() != null) {
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
            } else {
                DB_del();
            }

        }

        public void DB_del () {
            db.collection(postInfo.getLocation()).document(postInfo.getDocid())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //내가 쓴 글의 목록에서도 삭제해준다. 올~ 안까먹고 해놨구나 기특한 내 과거
                            db.collection("USER").document(postInfo.getId()).collection("MyPosts").document(postInfo.getDocid()).delete();
                            Toast("삭제되었습니다.");
                            GoBack(DELETE_RESULT, postInfo.getDocid());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast("삭제 실패하였습니다. :db");
                        }
                    });
        }

        public void GoBack ( int result, String docid){ //게시물번호를 넘겨주고 frag에서 처리하기위함.
            Intent intent = new Intent();
            intent.putExtra("docid", docid);
            setResult(result, intent);
            finish();
        }


        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onOptionsItemSelected (@NonNull MenuItem item){
            switch (item.getItemId()) {
                case R.id.delete:
                    PostDelete(0);
                    break;
//                case R.id.scrap:
//                    Toast("스크랩되었습니다.(아직 미구현)");
//                    break;
                case R.id.submission:
                    Toast("신고되었습니다.");
                    Declaration declaration = new Declaration(postInfo.getDocid(),postInfo.getId(),postInfo.getContents());
                    FirebaseFirestore.getInstance().collection("Declaration").document().set(declaration);
                    break;
                case R.id.Letter:
//                Toast("쪽지를 보냅니다. (to_"+postInfo.getPublisher()+")");
                    StartActivity(this, postInfo.getPublisher(), postInfo.getId());
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
        public void onBackPressed () {

            if (PostcommentsHolder != null) {
                commentsAdapter.Off_CommentbodyColor(PostcommentsHolder);
                PostcommentsHolder = null;
            } else {
                if (ACTION) { //좋아요 버튼 눌렀으면 리스트 리셋
                    GoBack(SOMETHING_IN_POST, postInfo.getDocid());
                } else {
                    finish();
                }
            }
        }

        public void hideKeyPad () {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.AddCommentT.getWindowToken(), 0);
        }

        public void ShowKeyPad () {
            binding.AddCommentT.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

        public void Toast (String str){
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }

        public void StartActivity (Activity activity, String receiver_publisher, String receiver_id)
        {
            Intent intent = new Intent(activity, ChatRoomActivity.class);
            intent.putExtra("user_nick", receiver_publisher);
            intent.putExtra("user_id", receiver_id);
            startActivity(intent);
        }

        private void Loading ( boolean ready){
            if (ready) {
                loader.setVisibility(View.VISIBLE); //로딩화면
                hideKeyPad(); //보기안좋으니까 키패드 내리고
            } else {
                loader.setVisibility(View.GONE); //로딩화면 제거
                binding.AddCommentT.setText(null); //댓글창 클리어

                if (PostcommentsHolder != null) {
                    commentsAdapter.Off_CommentbodyColor(PostcommentsHolder);
                    PostcommentsHolder = null;
                }
            }
        }

    }
