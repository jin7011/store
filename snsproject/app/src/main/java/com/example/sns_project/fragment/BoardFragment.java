package com.example.sns_project.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
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
    private RecyclerView recyclerView;
    private String location;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipe;
    private static final int DOWN_SROLLED = 0;
    private static final int UP_SROLLED = 1;
    private static final int Upload_Limit = 20;

    public BoardFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("onAttach","onAttach");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { //view가 완전히 완료된 이후에 나오는 메서드라서 이곳에 findby~ 써야 안전함
        super.onViewCreated(view, savedInstanceState);

        swipe = view.findViewById(R.id.swipe);

        Bundle bundle = getArguments();
        location = bundle.getString("location");

        if(postList.size() == 0){
            RecyclerInit(getActivity(),view);
            Log.d("zz","리사이클러뷰 이닛");
        }

        RecyclerView_ScrollListener();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        return view;
    }

    private void RecyclerInit(Activity activity,View view) {//저장된 db에서 내용을 뽑아오는 로직

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        recyclerView = (RecyclerView)view.findViewById(R.id.RecyclerView_frag);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setItemPrefetchEnabled(true); //렌더링 퍼포먼스 향상
        recyclerView.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(activity,postList); //처음엔 비어있는 list를 넣어줬음
//        postAdapter.setHasStableIds(true); 이걸쓰면 게시물 시간이 재사용되서 리셋이 안되는 이슈가 발생
        recyclerView.setAdapter(postAdapter);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        postUpdate(UP_SROLLED); //여기서 리스트를 채우고 갱신 (위로 갱신)

    }

    public void RecyclerView_ScrollListener(){

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) { //아래 갱신
                super.onScrolled(recyclerView, dx, dy);
                        if (!recyclerView.canScrollVertically(1)) { //끝에 도달하면 추가
                            postUpdate(DOWN_SROLLED);
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
                        //swipe 업데이트는 다르게 해줄 필요가 있는게 당기면 위로 정보가 업데이트 되어야 하는데 지금같은 경운 date를 기반으로 아래로 새로고침되니까 새로운 글이 나오지않음
                        //swipe를 할경우 기존의 List의 뒷부분은 다 지우고 앞부분부터 새로 받아와야함
                        //반면에 삭제는 가지고 있는 리스트를 그대로 유지하고 내가 쓴 글의 position만 지우고 갱신함.
                        removeScrollPullUpListener();
                        postUpdate(UP_SROLLED);

                        Snackbar.make(recyclerView,"새로고침 되었습니다.",Snackbar.LENGTH_SHORT).show();
                        swipe.setRefreshing(false);
                    }
                },1000);
            }
        });
    }

    public void postUpdate(int request){

        if(request == DOWN_SROLLED)
            DownScrolled();
        else
            UpScrolled();

    }

    private void UpScrolled() {

        Date date = new Date();
        ArrayList<PostInfo> newPosts = new ArrayList<>();

        db.collection(location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", date)
                .limit(Upload_Limit)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("가져옴", document.getId() + " => " + document.getData());
                                newPosts.add(new PostInfo(
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
                            if(newPosts.size() < Upload_Limit){
                                newPosts.addAll(postList);
                            }
                            postAdapter.PostInfoDiffUtil(newPosts);
                            recyclerView.smoothScrollToPosition(0);
                            postList.clear();
                            postList.addAll(newPosts);
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void DownScrolled(){

        Date date = postList.size() == 0 ? new Date() : postList.get(postList.size() - 1).getCreatedAt();

        db.collection(location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", date)
                .limit(Upload_Limit)
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

                            postAdapter.PostInfoDiffUtil(postList);
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