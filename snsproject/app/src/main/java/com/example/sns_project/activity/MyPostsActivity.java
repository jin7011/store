package com.example.sns_project.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.sns_project.Adapter.MyPostsAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_PostList;
import com.example.sns_project.databinding.ActivityMyPostsBinding;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import static com.example.sns_project.util.Named.VERTICAL;

public class MyPostsActivity extends AppCompatActivity {
    ActivityMyPostsBinding binding;

    private LiveData_PostList liveData_postList;
    private MyPostsAdapter adapter;
    private RecyclerView recyclerView;
    private My_Utility my_utility;
    private PostControler postControler = new PostControler();
    private Toolbar toolbar;
    private RelativeLayout loaderView;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loaderView = findViewById(R.id.loaderLyaout);

        RecyclerInit();
        liveData_postList = new ViewModelProvider(this).get(LiveData_PostList.class);
        liveData_postList.get().observe(this, new Observer<ArrayList<PostInfo>>() {
            @Override
            public void onChanged(ArrayList<PostInfo> postInfos) {
                adapter.PostInfoDiffUtil(postInfos);
                loaderView.setVisibility(View.GONE);
            }
        });
        Bring_MyPosts();
        Set_ToolBar();
    }

    private void Set_ToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_MyPosts);
        setSupportActionBar(toolbar);
        binding.toolbarTitleMyPosts.setText("내가 쓴 글");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void RecyclerInit(){
        recyclerView = binding.MyPostsRecyclerView;
        adapter = new MyPostsAdapter(this);
        my_utility = new My_Utility(this,recyclerView,adapter);
        my_utility.RecyclerInit(VERTICAL);
    }

    private void Bring_MyPosts(){

        loaderView.setVisibility(View.VISIBLE);

        postControler.Bring_MyPosts(user.getUid(), new PostControler.Listener_CompletePostInfos() {
            @Override
            public void onComplete_Get_PostsArrays(ArrayList<PostInfo> NewPostInfos) {
                for(PostInfo p : NewPostInfos)
                    Log.d("zoz23",p.getContents()+"");
                Log.d("zoz23",user.getUid()+"");
                liveData_postList.get().setValue(NewPostInfos);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}