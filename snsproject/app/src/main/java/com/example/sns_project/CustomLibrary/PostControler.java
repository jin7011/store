package com.example.sns_project.CustomLibrary;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.Listener.Listener_CompletePostInfos;
import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.info.RecommentInfo;
import com.example.sns_project.util.My_Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.example.sns_project.util.Named.UPLOAD_LIMIT;

/**
 * use this with my_utility for requesting posts
 * in recycler_init() after my utility or in same time
 */

public class PostControler {

    private String post_location;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private My_Utility my_utility;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    public PostControler(String post_location,My_Utility my_utility){
        this.post_location = post_location;
        this.my_utility = my_utility;
        this.recyclerView = my_utility.getRecyclerView();
        this.adapter = my_utility.getAdapter();
    }

    public void Search_Post(String KeyWord, Listener_CompletePostInfos listener_completePostInfos){

        Date newdate = new Date();
        ArrayList<PostInfo> newPosts = new ArrayList<>();
        Log.d("zozozozozo","시작: "+newPosts.size());

        db.collection(post_location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", newdate)//업스크롤 효과 (위에서부터 최신상태로)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String nickname =  document.get("publisher").toString();
                                String content =  document.get("contents").toString();
                                String title = document.get("title").toString();

                                if(nickname.contains(KeyWord) || content.contains(KeyWord) || title.contains(KeyWord)) {

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
                                                    Integer.parseInt(document.get("good").toString()), Integer.parseInt(document.get("comment").toString()), post_location,
                                                    (ArrayList<String>) document.getData().get("storagepath"), commentInfoArrayList,
                                                    (HashMap<String, Integer>) document.getData().get("good_user")
                                            )
                                    );
                                    Log.d("zozozozozo","찾은거: "+title);
                                }

                            }///////////////////////////////////////////////////////////////////////완료
                            Log.d("zozozozozo","끝: "+newPosts.size());
                            listener_completePostInfos.onComplete(newPosts);

//                            2021-05-04 22:46:56.748 10381-10381/com.example.sns_project D/zozozozozo: 시작: 0
//                            2021-05-04 22:46:57.269 10381-10381/com.example.sns_project D/zozozozozo: 찾은거: zxczxcz
//                            2021-05-04 22:46:57.270 10381-10381/com.example.sns_project D/zozozozozo: 찾은거: asdasd
//                            2021-05-04 22:46:57.275 10381-10381/com.example.sns_project D/zozozozozo: 끝: 2
//                            Snackbar.make(recyclerView,"새로고침 되었습니다.",Snackbar.LENGTH_SHORT).show();
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void Request_NewPosts(Listener_CompletePostInfos listener_completePostInfos){

        //최신글 20개를 가져옴.
        Date NewDate = new Date();
        ArrayList<PostInfo> newPosts = new ArrayList<>();
        Log.d("zozozozozo","시작: "+newPosts.size());

        db.collection(post_location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", NewDate)//업스크롤 효과 (위에서부터 최신상태로)
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
                                                Integer.parseInt(document.get("good").toString()), Integer.parseInt(document.get("comment").toString()), post_location,
                                                (ArrayList<String>) document.getData().get("storagepath"), commentInfoArrayList,
                                                (HashMap<String, Integer>) document.getData().get("good_user")
                                        )
                                );
                            }///////////////////////////////////////////////////////////////////////완료
                            Log.d("zozozozozo","끝: "+newPosts.size());
                            listener_completePostInfos.onComplete(newPosts);
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    public void Request_AfterPosts(ArrayList<PostInfo> postlist,Date OldestDate,Listener_CompletePostInfos listener_completePostInfos){

        //시간을 기점으로 이후의 게시물 20개를 가져옴 (NextPosts라고 하려다가 New랑 헷갈려서 After로 바꿈)
        ArrayList<PostInfo> newPosts = new ArrayList<>();
        ArrayList<PostInfo> temp = deepCopy(postlist);

        Log.d("zozozozozo","시작: "+newPosts.size());

        db.collection(post_location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", OldestDate)//업스크롤 효과 (위에서부터 최신상태로)
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
                                                Integer.parseInt(document.get("good").toString()), Integer.parseInt(document.get("comment").toString()), post_location,
                                                (ArrayList<String>) document.getData().get("storagepath"), commentInfoArrayList,
                                                (HashMap<String, Integer>) document.getData().get("good_user")
                                        )
                                );
                            }///////////////////////////////////////////////////////////////////////완료
                            Log.d("zozozozozo","끝: "+newPosts.size());
                            temp.addAll(newPosts);
                            listener_completePostInfos.onComplete(temp);
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void Update_ThePost(ArrayList<PostInfo> postList,String docid,Listener_CompletePostInfos listener_completePostInfos) {

        ArrayList<PostInfo> temp = deepCopy(postList);

        boolean flag = false;
        int idx = 0;

        for (int x = 0; x < postList.size(); x++) { //해당 게시물의 position을 찾고,
            PostInfo postInfo = postList.get(x);
            if (postInfo.getDocid().equals(docid)) {
                idx = x;
                flag = true;
                break;
            }
        }

        if(flag) { //해당 게시물이 로드되어있던 거라면, 그것만 갱신
            final PostInfo[] newpostInfo = new PostInfo[1];
            int finalIdx = idx; //위 2줄은 비동기랑 맞추려고 어쩔수없이

            db.collection(post_location).document(docid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                                Integer.parseInt(document.get("good").toString()), Integer.parseInt(document.get("comment").toString()), post_location,
                                (ArrayList<String>) document.getData().get("storagepath"), commentInfoArrayList,
                                (HashMap<String, Integer>) document.getData().get("good_user")
                        );
                        temp.remove(finalIdx);
                        temp.add(finalIdx, newpostInfo[0]);
                        listener_completePostInfos.onComplete(temp);
                    }
                }
            });
        }

    }

    public void Delete_ThePost(ArrayList<PostInfo> postList,String docid,Listener_CompletePostInfos listener_completePostInfos){

        for(int x =0; x<postList.size(); x++){ //현재 제공되어 있는 리스트에 삭제한 해당 게시물이 존재한다면 간편하게 그것만 제외하고 리셋(깔끔하고 비용이 적게든다고 생각했음)
            if(postList.get(x).getDocid().equals(docid)){
                ArrayList<PostInfo> temp;
                temp = deepCopy(postList);
                temp.remove(x);
                listener_completePostInfos.onComplete(temp);
                break;
            }
        }

    }

    public ArrayList<PostInfo> deepCopy(ArrayList<PostInfo> oldone){

        ArrayList<PostInfo> newone = new ArrayList<>();

        for(int x=0; x<oldone.size(); x++)
            newone.add(new PostInfo(oldone.get(x)));

        return newone;
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

}