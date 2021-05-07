package com.example.sns_project.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.Listener.Listener_CompletePostInfos;
import com.example.sns_project.data.LiveData_PostList;
import com.example.sns_project.databinding.ActivitySearchBinding;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.My_Utility;

import java.util.ArrayList;

import static com.example.sns_project.util.Named.SOMETHING_IN_POST;
import static com.example.sns_project.util.Named.VERTICAL;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private String post_location;
    private My_Utility my_utility;
    private LiveData_PostList PostListModel;
    private PostControler postControler ;
    private PostAdapter adapter;
    private  RecyclerView recyclerView;
    private ArrayList<PostInfo> Loaded_Posts= new ArrayList<>();
    private String KeyWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PostListModel = new ViewModelProvider(SearchActivity.this).get(LiveData_PostList.class);
        PostListModel.get().observe(this, new Observer<ArrayList<PostInfo>>() {
            @Override
            public void onChanged(ArrayList<PostInfo> postInfos) {
                adapter.PostInfoDiffUtil(postInfos);
                Loaded_Posts.clear();
                Loaded_Posts.addAll(postInfos);
            }
        });

        binding.searchET.setQueryHint("제목/내용/글쓴이");
        binding.searchET.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                //검색을 통해서 처음으로 리스트를 만들고, 리사이클러뷰의 데이터를 생성하는 부분,
                Loaded_Posts.clear();
                adapter.NoMore_Load(false);
                KeyWord = keyword;
                hideKeyPad();

                postControler.Search_Post(Loaded_Posts,KeyWord, new Listener_CompletePostInfos() { //아무것도 없는 상태에서 처음 검색.
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
                            First_Search();//아무것도 없는 상태에서 처음 검색.
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
        adapter = new PostAdapter(this, new PostAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                adapter.setProgressMore(true);
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DownScrolled(); //처음 검색한 이후에 스크롤을 내리면서 추가로 데이터를 받아올 때
                    }
                }, 1000);
            }
        });
        my_utility = new My_Utility(this,recyclerView,adapter);
        my_utility.RecyclerInit(VERTICAL);
    }

    private void DownScrolled() {
        //몇 개가 결과로 나오든 간에 애매하게 나오면 결과가 0개가 될 때까지 다운스크롤을 화면을 채울 때까지 진행한다.
        postControler.Search_Post(Loaded_Posts, KeyWord, new Listener_CompletePostInfos() {
            @Override
            public void onComplete(ArrayList<PostInfo> NewPostInfos) {
                Log.d("다운스크롤써치",""+(NewPostInfos.size()-Loaded_Posts.size()));
                adapter.NoMore_Load( (NewPostInfos.size() - Loaded_Posts.size()) == 0 );
                PostListModel.get().setValue(NewPostInfos);
                //그러므로 결과가 0개여도 null값이 제거되서 나온 결과인 NewPostInfos를 한번 디프해줘야 로딩창이 사라진다. (기존 boardfrag와 로직이 같음)
            }
        });
    }

    private void First_Search(){
        postControler.Search_Post(Loaded_Posts, KeyWord, new Listener_CompletePostInfos() {
            @Override
            public void onComplete(ArrayList<PostInfo> NewPostInfos) {

                if(NewPostInfos.size() == 0) {
                    binding.SearchRecyclerView.setVisibility(View.GONE);
                    binding.cannotfindConstranint.setVisibility(View.VISIBLE);
                    adapter.NoMore_Load(true);
                }
                else {
                    binding.SearchRecyclerView.setVisibility(View.VISIBLE);
                    binding.cannotfindConstranint.setVisibility(View.GONE);
                    PostListModel.get().setValue(NewPostInfos);
                    adapter.NoMore_Load(false);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == SOMETHING_IN_POST) { //좋아요/(댓글추가) 리턴값
            String docid = null;
            if (data != null) {
                docid = data.getStringExtra("docid");
            }
            Log.d("From PostActivity","requestCode: "+requestCode+" docid: "+docid);
            postControler.Update_ThePost(Loaded_Posts, docid, new Listener_CompletePostInfos() {
                @Override
                public void onComplete(ArrayList<PostInfo> NewPostInfos) {
                    PostListModel.get().setValue(NewPostInfos);
                }
            });
        }
    }

    public void hideKeyPad(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.searchET.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}