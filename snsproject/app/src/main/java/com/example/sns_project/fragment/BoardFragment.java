package com.example.sns_project.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_PostList;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.Named;
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

import static com.example.sns_project.util.Named.DOWN_SROLLED;
import static com.example.sns_project.util.Named.DeleteResult;
import static com.example.sns_project.util.Named.GoodResult;
import static com.example.sns_project.util.Named.UP_SROLLED;
import static com.example.sns_project.util.Named.Upload_Limit;
import static com.example.sns_project.util.Named.WriteResult;

public class BoardFragment extends Fragment {
    //리스트를 라이브에서 관리하고 옵저버로 리사이클러뷰와 리스트를 관리함
    //여러기능에 대해서 깊은복사를 사용해서 기존의 리스트정보를 최대한 유지하면서 수정하는 부분만 건드림으로써
    //기능을 실행할 때에는 조금 비효율적이지만, 한번 갱신한 자료는 다시 갱신하지 않아도 되고, 스크롤이 유지된다. 4월3일

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<PostInfo> postList; //fragment에서 갱신하는 임시리스트
    private LiveData_PostList PostListModel; //postList 임시리스트를 라이브자료에 넣음으로써 리사이클러뷰를 갱신함
    private Observer<ArrayList<PostInfo>> PostList_Observer;
    private RecyclerView recyclerView;
    private String location;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipe;
    private Named named = new Named();

    public BoardFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PostListModel.get().removeObserver(PostList_Observer); //fragment destroy되었을 때는 livedata의 옵저버를 해체시켜주자 (따로 라이프사이클없이 계속돌아가게 해놨음)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { //view가 완전히 완료된 이후에 나오는 메서드라서 이곳에 findby~ 써야 안전함
        super.onViewCreated(view, savedInstanceState);

        swipe = view.findViewById(R.id.swipe);

        Bundle bundle = getArguments();
        location = bundle.getString("location");
        PostListModel = new ViewModelProvider(getActivity()).get(LiveData_PostList.class);
        postList = PostListModel.getPostList();

        if(postList.size() == 0){
            RecyclerInit(getActivity(),view);
            Log.d("zz","리사이클러뷰 이닛");
        }

        //라이브데이터
        PostList_Observer = new Observer<ArrayList<PostInfo>>() { // 따로 라이프사이클없이 계속돌아가게 해놨음
            @Override
            public void onChanged(ArrayList<PostInfo> postInfos) {
                postAdapter.PostInfoDiffUtil(postInfos);
                PostListModel.setpostList(postInfos);
                postList.clear();
                postList.addAll(PostListModel.getPostList());
                Log.d("zxczxc", "newPosts: " + postInfos.size());
                Log.d("zxczxc", "PostListModel.getPostList: " + PostListModel.getPostList().size());
                Log.d("zxczxc", "postList: " + postList.size());
            }
        };
        PostListModel.get().observeForever(PostList_Observer);

        RecyclerView_ScrollListener();

    }
    //추가
    //삭제
    //댓글/좋아요
    //새로고침(이전꺼좋으니까 수정만)
    public void postUpdate(int request,String docid){

        if(request == DOWN_SROLLED) //아래로 새로고침할 때
            DownScrolled();
        else if(request == UP_SROLLED || request == WriteResult) //위로 새로고침하거나 글쓰고 왔을 때
            UpScrolled();
        else if(request == GoodResult) // 다른 게시물에 좋아요버튼 누르고 왔을 때
            GoodPressed(docid);
        else if(request == DeleteResult){ // 내 게시물을 삭제하고 왔을 때
            Deleted(docid);
        }

    }
    //todo 댓글을 달고오면 리셋 ( 한마디로 포스트내에서 뭔가를 하고 오면 리셋 아니면 굳이 보고온 것만으로는 갱신 x )

    private void GoodPressed(String docid) { //좋아요

        //(좋아요 누르고 나옴) 스크롤을 가능한 유지하고, 리스트 상태를 새로 고침.
        //아무래도 리셋할거 없이 해당 포지션을 어댑터에서 전달하고 그걸actvity에서 받아와서 수정한다음 이쪽으로
        //넘겨주고 그것만 처리하는게 깔끔할 듯. 그 이후에 diffutil사용

        boolean flag = false;

        for(int x =0; x<postList.size(); x++){
            // 좋아요의 경우 보이기엔 그냥 +1로 해주자 새로 갱신해줄만큼 가치있지않음
            // 보통 좋아요 누르면 +1 되는거보고 그냥 가니까, 그게 아니라 궁금하면 새로고침했을 때 db에서 좋아요 불러오므로 확실하게 확인가능.
            //todo 사용자가 많아지면 포스트에서 어떠한 활동이라도 하고 나오면 해당 게시물만이 아니라 전부 리셋해주는 경우가 필요할 듯 그 경우는 actedOnPost같은 함수 만들어서 하나에 다 집어넣는게 좋을듯
            PostInfo postInfo = postList.get(x);
            if(postInfo.getDocid().equals(docid)){
                ArrayList<PostInfo> temp;
                temp = deepCopy(postList);
                temp.get(x).setGood(temp.get(x).getGood()+1);
                PostListModel.get().setValue(temp);
                flag = true;
                break;
            }
        }

        if(!flag) //게시물을 검색해서 찾은 경우 그냥 리셋해주는게 좋다
            UpScrolled();
    }

    private void Deleted(String docid) { //삭제
        //(todo)삭제와 좋아요는 리사이클러뷰의 위치를 유지시켜주자.
        boolean flag = false;

        for(int x =0; x<postList.size(); x++){ //현재 제공되어 있는 리스트에 삭제한 해당 게시물이 존재한다면 간편하게 그것만 제외하고 리셋(깔끔하고 비용이 적게든다고 생각했음)
            if(postList.get(x).getDocid().equals(docid)){
                ArrayList<PostInfo> temp;
                temp = deepCopy(postList);
                temp.remove(x);
                PostListModel.get().setValue(temp);
                flag = true;
                break;
            }
        }

        if(!flag) //삭제한 게시글이 당장 리스트에 보이지 않는다면(아마도 올린지 좀 된 글의 경우 -> 보통 검색으로 자신의 게시물을 찾아서 삭제한경우) 그냥 리셋 4/1일버전에서는 작동할 일이 없을 것으로 보임.
            UpScrolled();
    }

    private void UpScrolled() { // (글생성/새로고침) 한계치만큼 지료를 받아와서 한계치보다 적으면 이전의 자료와 덮어씌우고, 최대치까지 끌어모았다면 원래list는 지우고 새것을 사용. -> 스크롤 맨위로

        Date newdate = new Date();
        ArrayList<PostInfo> newPosts = new ArrayList<>();

        if(postList.size() != 0) {
            //이미 리스트가 있고, 새로받아오는 리스트가 limit미만이라면, 그냥 덧붙이자.
            Date olddate;

            olddate = postList.get(0).getCreatedAt();
            db.collection(location)
                    .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", newdate).whereGreaterThan("createdAt",olddate)
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
                                //////////////////////////////////////////////////////////////////
                                if(newPosts.size() < Upload_Limit){ //20미만
                                    newPosts.addAll(postList);
                                    PostListModel.get().setValue(newPosts);
                                }else{ // 20개
                                    PostListModel.get().setValue(newPosts);
                                }
                                recyclerView.smoothScrollToPosition(0);
                            } else {
                                Log.d("실패함", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }else{
            db.collection(location)
                    .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", newdate)
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
                                PostListModel.get().setValue(newPosts);
                                recyclerView.smoothScrollToPosition(0);
                            } else {
                                Log.d("실패함", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    public void DownScrolled(){

        ArrayList<PostInfo> newPosts = new ArrayList<>();
        ArrayList<PostInfo> temp = deepCopy(postList);
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
                            temp.addAll(newPosts);
                            PostListModel.get().setValue(temp);
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void RecyclerInit(Activity activity,View view) {//저장된 db에서 내용을 뽑아오는 로직

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        recyclerView = (RecyclerView)view.findViewById(R.id.RecyclerView_frag);

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setItemPrefetchEnabled(true); //렌더링 퍼포먼스 향상
        recyclerView.setLayoutManager(layoutManager);
        postAdapter = new PostAdapter(activity); //처음엔 비어있는 list를 넣어줬음
//        postAdapter.setHasStableIds(true); 이걸쓰면 게시물 시간이 재사용되서 리셋이 안되는 이슈가 발생
//        postAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY); //스크롤 저장하는건데 필요없어짐
        recyclerView.setAdapter(postAdapter);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        UpScrolled(); //여기서 리스트를 채우고 갱신 (위로 갱신)

    }

    public void RecyclerView_ScrollListener(){

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) { //아래 갱신
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) { //끝에 도달하면 추가
                    DownScrolled();
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
                        UpScrolled();

                        Snackbar.make(recyclerView,"새로고침 되었습니다.",Snackbar.LENGTH_SHORT).show();
                        swipe.setRefreshing(false);
                    }
                },1000);
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

    public ArrayList<PostInfo> deepCopy(ArrayList<PostInfo> oldone){

        ArrayList<PostInfo> newone = new ArrayList<>();

        for(int x=0; x<oldone.size(); x++)
            newone.add(new PostInfo(oldone.get(x)));

        return newone;
    }

}