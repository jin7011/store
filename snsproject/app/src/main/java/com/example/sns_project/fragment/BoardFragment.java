package com.example.sns_project.fragment;

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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_PostList;
import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.info.RecommentInfo;
import com.example.sns_project.util.My_Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.example.sns_project.util.Named.DOWN_SROLLED;
import static com.example.sns_project.util.Named.DELETE_RESULT;
import static com.example.sns_project.util.Named.SOMETHING_IN_POST;
import static com.example.sns_project.util.Named.UP_SROLLED;
import static com.example.sns_project.util.Named.UPLOAD_LIMIT;
import static com.example.sns_project.util.Named.VERTICAL;
import static com.example.sns_project.util.Named.WRITE_RESULT;

public class BoardFragment extends Fragment {
    //리스트를 라이브에서 관리하고 옵저버로 리사이클러뷰와 리스트를 관리함
    //여러기능에 대해서 깊은복사를 사용해서 기존의 리스트정보를 최대한 유지하면서 수정하는 부분만 건드림으로써
    //기능을 실행할 때에는 조금 비효율적이지만, 한번 갱신한 자료는 다시 갱신하지 않아도 되고, 스크롤이 유지된다. 4월3일

    private FirebaseAuth mAuth;
    private MyAccount myAccount;
    private FirebaseFirestore db;
    private ArrayList<PostInfo> postList; //fragment에서 갱신하는 임시리스트
    private LiveData_PostList PostListModel; //postList 임시리스트를 라이브자료에 넣음으로써 리사이클러뷰를 갱신함
    private Observer<ArrayList<PostInfo>> PostList_Observer;
    private RecyclerView recyclerView;
    private String location;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipe;
    private My_Utility my_utility;

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
        return inflater.inflate(R.layout.fragment_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { //view가 완전히 완료된 이후에 나오는 메서드라서 이곳에 findby~ 써야 안전함
        super.onViewCreated(view, savedInstanceState);
        swipe = view.findViewById(R.id.swipe_board);

        Bundle bundle = getArguments();
        myAccount = (MyAccount)bundle.getParcelable("Myaccount");
        location = myAccount.getLocation();
        PostListModel = new ViewModelProvider(getActivity()).get(LiveData_PostList.class);
        postList = PostListModel.getPostList();

        //라이브데이터
        PostList_Observer = new Observer<ArrayList<PostInfo>>() { // 따로 라이프사이클없이 계속돌아가게 해놨음
            @Override
            public void onChanged(ArrayList<PostInfo> postInfos) {
                if(postInfos != null && postAdapter != null) {
                    postAdapter.PostInfoDiffUtil(postInfos);
                    PostListModel.setpostList(postInfos);
                    postList.clear();
                    postList.addAll(PostListModel.getPostList());
                    Log.d("zxczxc", "newPosts: " + postInfos.size());
                    Log.d("zxczxc", "PostListModel.getPostList: " + PostListModel.getPostList().size());
                    Log.d("zxczxc", "postList: " + postList.size());
                }
            }
        };
        PostListModel.get().observeForever(PostList_Observer);

        RecyclerInit(view);
        Log.d("zz","리사이클러뷰 이닛");

        RecyclerView_ScrollListener();

    }
    //추가
    //삭제
    //댓글/좋아요
    //새로고침(이전꺼좋으니까 수정만)
    public void postUpdate(int request,String docid){

        if(request == DOWN_SROLLED) //아래로 새로고침할 때
            DownScrolled();
        else if(request == UP_SROLLED || request == WRITE_RESULT) //위로 새로고침하거나 글쓰고 왔을 때
            UpScrolled();
        else if(request == SOMETHING_IN_POST) // 다른 게시물에 좋아요버튼 누르고 왔을 때
            Good_or_Comment(docid);
        else if(request == DELETE_RESULT){ // 내 게시물을 삭제하고 왔을 때
            Deleted(docid);
        }

    }

    private void Good_or_Comment(String docid) { //좋아요

        //(좋아요 누르고 나옴) 스크롤을 가능한 유지하고, 리스트 상태를 새로 고침.
        //아무래도 리셋할거 없이 해당 포지션을 어댑터에서 전달하고 그걸actvity에서 받아와서 수정한다음 이쪽으로
        //넘겨주고 그것만 처리하는게 깔끔할 듯. 그 이후에 diffutil사용
        boolean flag = false;
        int idx = 0;

        for(int x =0; x<postList.size(); x++) { //해당 게시물의 position을 찾고,
            PostInfo postInfo = postList.get(x);
            if(postInfo.getDocid().equals(docid)){
                idx = x;
                flag = true;
                break;
            }
        }

        if(flag){ //해당 게시물이 로드되어있던 거라면, 그것만 갱신
            final PostInfo[] newpostInfo = new PostInfo[1];
            int finalIdx = idx; //위 2줄은 비동기랑 맞추려고 어쩔수없이
            db.collection(location).document(docid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        ArrayList<CommentInfo> commentInfoArrayList = get_commentArray_from_Firestore(document);
                        newpostInfo[0] = new PostInfo(
                                document.get("id").toString(),
                                document.get("publisher").toString(),
                                document.get("title").toString(),
                                document.get("contents").toString(),
                                (ArrayList<String>) document.getData().get("formats"),
                                new Date(document.getDate("createdAt").getTime()),
                                document.getId(),
                                Integer.parseInt(document.get("good").toString()), Integer.parseInt(document.get("comment").toString()), location,
                                (ArrayList<String>) document.getData().get("storagepath"),commentInfoArrayList,
                                (HashMap<String, Integer>)document.getData().get("good_user")
                        );
                        ArrayList<PostInfo> temp;
                        temp = deepCopy(postList);
                        temp.remove(finalIdx);
                        temp.add(finalIdx,newpostInfo[0]);
                        PostListModel.get().setValue(temp);
                    }
                }
            });
        }

    }

    private void Deleted(String docid) { //삭제

        for(int x =0; x<postList.size(); x++){ //현재 제공되어 있는 리스트에 삭제한 해당 게시물이 존재한다면 간편하게 그것만 제외하고 리셋(깔끔하고 비용이 적게든다고 생각했음)
            if(postList.get(x).getDocid().equals(docid)){
                ArrayList<PostInfo> temp;
                temp = deepCopy(postList);
                temp.remove(x);
                PostListModel.get().setValue(temp);
                break;
            }
        }
    }

    public void UpScrolled() {
        // (글생성/새로고침) 스크롤 맨위로

        Date newdate = new Date();
        ArrayList<PostInfo> newPosts = new ArrayList<>();

        db.collection(location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", newdate)
                .limit(UPLOAD_LIMIT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                ArrayList<CommentInfo> commentInfoArrayList = get_commentArray_from_Firestore(document);

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
                                                //아무래도 여기서 데이터 가져올때 형변환이 제대로 안된거같은 기분이 든다.
                                                (ArrayList<String>) document.getData().get("storagepath"),commentInfoArrayList,
                                                (HashMap<String, Integer>)document.getData().get("good_user")
                                        )
                                );
                            }
                            PostListModel.get().setValue(newPosts);
                            recyclerView.smoothScrollToPosition(0);
//                            Snackbar.make(recyclerView,"새로고침 되었습니다.",Snackbar.LENGTH_SHORT).show();
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });

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

    public ArrayList<RecommentInfo> get_RecommentArray_from_commentsmap(HashMap<String, Object> commentsmap ){
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


    public void DownScrolled(){
        ArrayList<PostInfo> newPosts = new ArrayList<>();
        ArrayList<PostInfo> temp = deepCopy(postList);
        Date date = postList.size() == 0 ? new Date() : postList.get(postList.size() - 1).getCreatedAt();

        db.collection(location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", date)
                .limit(UPLOAD_LIMIT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                ArrayList<CommentInfo> commentInfoArrayList = get_commentArray_from_Firestore(document);

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
                                                (ArrayList<String>) document.getData().get("storagepath"),commentInfoArrayList,
                                        (HashMap<String, Integer>)document.getData().get("good_user")
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

    private void RecyclerInit(View view) {//저장된 db에서 내용을 뽑아오는 로직

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = (RecyclerView)view.findViewById(R.id.RecyclerView_frag);
        postAdapter = new PostAdapter(getActivity()); //처음엔 비어있는 list를 넣어줬음
        my_utility = new My_Utility(getActivity(),recyclerView,postAdapter);
        my_utility.RecyclerInit(VERTICAL);
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
                        removeScrollPullUpListener();
                        UpScrolled();
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