package com.example.sns_project.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.Listener.Listener_CompletePostInfos;
import com.example.sns_project.R;
import com.example.sns_project.databinding.ActivitySearchBinding;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static com.example.sns_project.util.Named.VERTICAL;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String post_location;
    private My_Utility my_utility;
    private PostControler postControler ;
    private PostAdapter adapter;
    private int curIDX = 0;
    private SwipeRefreshLayout swipe;
    private  RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.searchET.setQueryHint("제목/내용/글쓴이");
        binding.searchET.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                curIDX = 0; //검색할 때마다 초기화해주자.
                hideKeyPad();

                postControler.Search_Post(keyword, new Listener_CompletePostInfos() {
                    @Override
                    public void onComplete(ArrayList<PostInfo> NewPostInfos) {
                        Log.d("plpl",""+ NewPostInfos.size());
                        if(NewPostInfos.size() == 0) {
                            binding.SearchRecyclerView.setVisibility(View.GONE);
                            binding.cannotfindConstranint.setVisibility(View.VISIBLE);
                        }
                        else {
                            binding.SearchRecyclerView.setVisibility(View.VISIBLE);
                            binding.cannotfindConstranint.setVisibility(View.GONE);
                            getmore(NewPostInfos);
                        }
                    }
                }); //리스트를 쫙 받아오고,
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        post_location = getIntent().getStringExtra("location");
        set_recycler();
        postControler = new PostControler(post_location,my_utility);

    }

    public void set_recycler(){
        recyclerView = binding.SearchRecyclerView;
        adapter = new PostAdapter(this);
        swipe = binding.swipeSearch;
        my_utility = new My_Utility(this,recyclerView,adapter);
        my_utility.RecyclerInit(VERTICAL);
    }

    public void getmore(ArrayList<PostInfo> postInfos){

        ArrayList<PostInfo> newposts = new ArrayList<>();

        for(int x = curIDX; (x<postInfos.size()) && (x<curIDX+20) ; x++){
            curIDX = x;
            newposts.add(postInfos.get(x));
        }

        adapter.PostInfoDiffUtil(newposts);
    }

    public void RecyclerView_ScrollListener(){

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) { //아래 갱신
//                super.onScrolled(recyclerView, dx, dy);
//                if (!recyclerView.canScrollVertically(1)) { //끝에 도달하면 추가
//                    DownScrolled();
//                }
//            }
//        });

        swipe.setColorSchemeResources(
                R.color.pantone
        );

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //swipe 업데이트는 다르게 해줄 필요가 있는게 당기면 위로 정보가 업데이트 되어야 하는데 지금같은 경운 date를 기반으로 아래로 새로고침되니까 새로운 글이 나오지않음
                        //swipe를 할경우 기존의 List의 뒷부분은 다 지우고 앞부분부터 새로 받아와야함
                        //반면에 삭제는 가지고 있는 리스트를 그대로 유지하고 내가 쓴 글의 position만 지우고 갱신함.
//                        removeScrollPullUpListener();
                        swipe.setRefreshing(false);
                    }
                },1000);
            }
        });
    }

    public void hideKeyPad(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.searchET.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}