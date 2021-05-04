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
