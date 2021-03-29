package com.example.sns_project.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.R;
import com.example.sns_project.info.PostInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class BoardFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private ArrayList<PostInfo> postList = new ArrayList<>();
    private  RecyclerView recyclerView;
    private String location;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipe;

    public BoardFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_board, container, false);
        swipe = view.findViewById(R.id.swipe);
        RecyclerInit(getActivity(),view);
        RecyclerView_ScrollListener();

        return view;
    }

    private void RecyclerInit(Activity activity,View view) {//저장된 db에서 내용을 뽑아오는 로직

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        recyclerView = (RecyclerView)view.findViewById(R.id.RecyclerView_frag);
        Bundle bundle = getArguments();

        location = bundle.getString("location");

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        postAdapter = new PostAdapter(activity, postList);
        recyclerView.setAdapter(postAdapter);

        postUpdate();

    }

    public void RecyclerView_ScrollListener(){

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!recyclerView.canScrollVertically(1)) { //끝에 도달하면 추가
                    postUpdate();
                }
            }
        });

        swipe.setColorSchemeResources(
                R.color.purple_500
        );

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postAdapter.clear();
                        postList.clear();
                        removeScrollPullUpListener();
                        postUpdate();
                        Snackbar.make(recyclerView,"새로고침 되었습니다.",Snackbar.LENGTH_SHORT).show();
                        swipe.setRefreshing(false);
                    }
                },1000);
            }
        });
    }

    public void postUpdate(){

        Date date = postList.size() == 0 ? new Date() : postList.get(postList.size() - 1).getCreatedAt();

        db.collection(location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt",date)
                .limit(20)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("가져옴", document.getId() + " => " + document.getData());
                                postList.add(new PostInfo(
                                                document.get("id").toString(),
                                                document.get("publisher").toString(),
                                                document.get("title").toString(),
                                                document.get("contents").toString(),
                                                (ArrayList<String>) document.getData().get("formats"),
                                                new Date(document.getDate("createdAt").getTime()),
                                                document.getId(),
                                                Integer.parseInt(document.get("good").toString()), Integer.parseInt(document.get("comment").toString()), location,
                                                (ArrayList<String>) document.getData().get("storagepath")
                                        )
                                );
                            }
                            postAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void removeScrollPullUpListener(){
        recyclerView.removeOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
    }

}