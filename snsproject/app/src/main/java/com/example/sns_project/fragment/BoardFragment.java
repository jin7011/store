package com.example.sns_project.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_PostList;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static com.example.sns_project.util.Named.DELETE_RESULT;
import static com.example.sns_project.util.Named.SOMETHING_IN_POST;
import static com.example.sns_project.util.Named.UP_SROLLED;
import static com.example.sns_project.util.Named.VERTICAL;
import static com.example.sns_project.util.Named.WRITE_RESULT;

public class BoardFragment extends Fragment {
    //리스트를 라이브에서 관리하고 옵저버로 리사이클러뷰와 리스트를 관리함
    //여러기능에 대해서 깊은복사를 사용해서 기존의 리스트정보를 최대한 유지하면서 수정하는 부분만 건드림으로써
    //기능을 실행할 때에는 조금 비효율적이지만, 한번 갱신한 자료는 다시 갱신하지 않아도 되고, 스크롤이 유지된다. 4월3일

    private FirebaseAuth mAuth;
    private MyAccount myAccount;
    private FirebaseFirestore db;
    private ArrayList<PostInfo> Loaded_Posts; //fragment에서 갱신하는 임시리스트
    private LiveData_PostList PostListModel; //postList 임시리스트를 라이브자료에 넣음으로써 리사이클러뷰를 갱신함
    private Observer<ArrayList<PostInfo>> PostList_Observer;
    private RecyclerView recyclerView;
    private String location;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipe;
    private My_Utility my_utility;
    private PostControler postControler;
    private boolean ISUPSCROLL = false;

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
        Loaded_Posts = PostListModel.getPostList();

        //라이브데이터
        PostList_Observer = new Observer<ArrayList<PostInfo>>() { // 따로 라이프사이클없이 계속돌아가게 해놨음
            @Override
            public void onChanged(ArrayList<PostInfo> postInfos) {
                if(postInfos != null && postAdapter != null) {
                    postAdapter.PostInfoDiffUtil(postInfos);
                    PostListModel.setpostList(postInfos);
                    Loaded_Posts.clear();
                    Loaded_Posts.addAll(PostListModel.getPostList());

                    if(ISUPSCROLL){
                        Log.d("아니왠왜농ㄴ","스크롤위로");
                        ISUPSCROLL = false;
                        recyclerView.smoothScrollToPosition(0);
                    }
//                    Log.d("zxczxc", "newPosts: " + postInfos.size());
//                    Log.d("zxczxc", "PostListModel.getPostList: " + PostListModel.getPostList().size());
//                    Log.d("zxczxc", "postList: " + Loaded_Posts.size());
                    for(int x=0; x<Loaded_Posts.size(); x++) {
                        for(int y=0; y<Loaded_Posts.get(x).getComments().size(); y++) {
                            Log.d("zxczxc", "postList: "+x+","+y+" : " + Loaded_Posts.get(x).getComments().get(y).getGood_user());
                        }
                    }
                }
            }
        };
        PostListModel.get().observeForever(PostList_Observer);

        RecyclerInit(view);
        RecyclerView_ScrollListener();
    }

    //추가
    //삭제
    //댓글/좋아요
    //새로고침(이전꺼좋으니까 수정만)
    public void PostUpdate(int request, String docid){

        if(request == UP_SROLLED || request == WRITE_RESULT) //위로 새로고침하거나 글쓰고 왔을 때
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
        postControler.Update_UniPost(Loaded_Posts, docid, new PostControler.Listener_CompletePostInfos() {
            @Override
            public void onComplete_Get_PostsArrays(ArrayList<PostInfo> NewPostInfos) {
                PostListModel.get().setValue(NewPostInfos);
            }
        });
    }

    private void Deleted(String docid) { //삭제

        postControler.Check_Deleted_ThePost(Loaded_Posts, docid, new PostControler.Listener_CompletePostInfos() {
            @Override
            public void onComplete_Get_PostsArrays(ArrayList<PostInfo> NewPostInfos) {
                PostListModel.get().setValue(NewPostInfos);
            }
        });
    }

    public void UpScrolled() {
        // (글생성/새로고침) 스크롤 맨위로
        postControler.Request_NewPosts(new PostControler.Listener_CompletePostInfos() {
            @Override
            public void onComplete_Get_PostsArrays(ArrayList<PostInfo> NewPostInfos) {
                ISUPSCROLL = true;
                postAdapter.NoMore_Load(false); //새로고침하면 false처리해서 포스트를 받을수 있게 하자(downscrol)
                PostListModel.get().setValue(NewPostInfos);
                Toast.makeText(getActivity(),"새로고침 되었습니다.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void DownScrolled(){
        postControler.Request_Posts(Loaded_Posts, new PostControler.Listener_CompletePostInfos() {
            @Override
            public void onComplete_Get_PostsArrays(ArrayList<PostInfo> NewPostInfos) {
                Log.d("새로가져온 글 갯수",": "+ (NewPostInfos.size()-Loaded_Posts.size()) );
                postAdapter.NoMore_Load( (NewPostInfos.size()-Loaded_Posts.size()) == 0); //새로가져온게 없다면, 다운스크롤했을 때, 리스너가 if문에서 막힘. 가져온게 하나라도 있다면 다름
                PostListModel.get().setValue(NewPostInfos);
            }
        });
    }

    private void RecyclerInit(View view) {//저장된 db에서 내용을 뽑아오는 로직

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = (RecyclerView)view.findViewById(R.id.RecyclerView_frag);
        postAdapter = new PostAdapter(getActivity(), new PostAdapter.OnLoadMoreListener_bottom() { //아래로 스크롤 되었을 때에,
            @Override
            public void onLoadMore() {
                postAdapter.setProgressMore(true);
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ///////이부분에을 자신의 프로젝트에 맞게 설정하면 됨
                        //다음 페이지? 내용을 불러오는 부분
                        DownScrolled();
                        //////////////////////////////////////////////////
                    }
                }, 1000);
            }
        }); //처음엔 비어있는 list를 넣어줬음
        my_utility = new My_Utility(getActivity(),recyclerView,postAdapter);
        my_utility.RecyclerInit(VERTICAL);
        postControler = new PostControler(location,my_utility);
        UpScrolled(); //여기서 리스트를 채우고 갱신 (위로 갱신)

    }

    public void RecyclerView_ScrollListener(){

        swipe.setColorSchemeResources(R.color.pantone);
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

}