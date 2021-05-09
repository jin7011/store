package com.example.sns_project.CustomLibrary;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.info.RecommentInfo;
import com.example.sns_project.util.My_Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.sns_project.util.Named.SEARCH_LIMIT;
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
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

    public PostControler(String post_location,My_Utility my_utility){
        this.post_location = post_location;
        this.my_utility = my_utility;
        this.recyclerView = my_utility.getRecyclerView();
        this.adapter = my_utility.getAdapter();
    }

    public PostControler(String post_location){
        this.post_location = post_location;
    }

    public interface Listener_Complete_Get_PostInfo{
        void onComplete_Get_PostInfo(PostInfo postInfo);
    }

    public interface Listener_Complete_Set_PostInfo {
        void onComplete_Set_PostInfo();
//        void onFailed();
    }

    public interface Listener_Complete_Set_PostInfo_Transaction {
        void onComplete_Set_PostInfo(PostInfo NewPostInfo);
        void onFailed();
    }

    public interface Listener_CompletePostInfos {
        void onComplete_Get_PostsArrays(ArrayList<PostInfo> NewPostInfos);
    }

    public interface Listener_Complete_GoodPress {
        void onComplete_Good_Press(PostInfo NewPostInfo);
        void onFailed();
        void AlreadyDone();
        void CannotSelf();
    }

    public void Search_Post(ArrayList<PostInfo> Loaded_Posts,String KeyWord, Listener_CompletePostInfos listener_completePostInfos){

        ArrayList<PostInfo> newPosts = new ArrayList<>();
        ArrayList<PostInfo> temp = DeepCopy_ArrayPostInfo(Loaded_Posts);

        Date OldestDate = Loaded_Posts.size() == 0 ? new Date() : Loaded_Posts.get(Loaded_Posts.size() - 1).getCreatedAt();

        Log.d("zozozozozo","시작: "+newPosts.size());

        db.collection(post_location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", OldestDate)//업스크롤 효과 (위에서부터 최신상태로)
                .limit(SEARCH_LIMIT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if(document.exists()) {
                                    String nickname = document.get("publisher").toString();
                                    String content = document.get("contents").toString();
                                    String title = document.get("title").toString();

                                    if (nickname.contains(KeyWord) || content.contains(KeyWord) || title.contains(KeyWord)) {

                                        Log.d("가져옴", document.getId() + " => " + document.getData());
                                        newPosts.add(new PostInfo(Get_PostInfo_From_Store(document)));
                                        Log.d("zozozozozo", "찾은거: " + title);
                                    }
                                }

                            }///////////////////////////////////////////////////////////////////////완료
                            Log.d("zozozozozo","끝: "+newPosts.size());
                            temp.addAll(newPosts);
                            listener_completePostInfos.onComplete_Get_PostsArrays(temp);
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

                                if(document.exists()) {
                                    Log.d("가져옴", document.getId() + " => " + document.getData());
                                    newPosts.add(new PostInfo(Get_PostInfo_From_Store(document)));
                                }

                            }///////////////////////////////////////////////////////////////////////완료
                            Log.d("zozozozozo","끝: "+newPosts.size());
                            listener_completePostInfos.onComplete_Get_PostsArrays(newPosts);
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    public void Request_Posts(ArrayList<PostInfo> Loaded_Posts, Listener_CompletePostInfos listener_completePostInfos){

        //시간을 기점으로 이후의 게시물 20개를 가져옴 (NextPosts라고 하려다가 New랑 헷갈려서 After로 바꿈)
        ArrayList<PostInfo> newPosts = new ArrayList<>();
        ArrayList<PostInfo> temp = DeepCopy_ArrayPostInfo(Loaded_Posts);

        Date OldestDate = Loaded_Posts.size() == 0 ? new Date() : Loaded_Posts.get(Loaded_Posts.size() - 1).getCreatedAt();

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

                                Log.d("가져옴", document.getId() + " => " + document.getData());
                                newPosts.add(new PostInfo( Get_PostInfo_From_Store(document) ) );

                            }///////////////////////////////////////////////////////////////////////완료
                            Log.d("zozozozozo","끝: "+newPosts.size());
                            temp.addAll(newPosts);
                            listener_completePostInfos.onComplete_Get_PostsArrays(temp);
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void Update_UniPost(ArrayList<PostInfo> postList, String docid, Listener_CompletePostInfos listener_completePostInfos) {

        ArrayList<PostInfo> temp = DeepCopy_ArrayPostInfo(postList);

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
                        if(document.exists()) {
                            newpostInfo[0] = Get_PostInfo_From_Store(document);
                            temp.remove(finalIdx);
                            temp.add(finalIdx, newpostInfo[0]);
                            listener_completePostInfos.onComplete_Get_PostsArrays(temp);
                        }
                    }

                }
            });
        }
    }

    public void Get_UniPost(String docid, Listener_Complete_Get_PostInfo listener_complete_get_postInfo){

        db.collection(post_location).document(docid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        PostInfo postInfo = Get_PostInfo_From_Store(documentSnapshot);
                        listener_complete_get_postInfo.onComplete_Get_PostInfo(postInfo);
                    }else{
                        my_utility.Toast("존재하지 않는 게시물입니다.");
                    }
            }
        });
    }

    public void Set_UniPost(PostInfo postInfo,Listener_Complete_Set_PostInfo listener_complete_set_postInfo){

        db.collection(post_location).document(postInfo.getDocid()).update(postInfo.getPostInfo()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                listener_complete_set_postInfo.onComplete_Set_PostInfo();
            }
        });
    }

    public void Update_ReComments_With_Transaction(String docid,String Key, RecommentInfo NewRecomment,Listener_Complete_Set_PostInfo_Transaction completeListener){

        DocumentReference drf =  db.collection(post_location).document(docid);

        db.runTransaction(new Transaction.Function<PostInfo>() {
            @Override
            public PostInfo apply(Transaction transaction) throws FirebaseFirestoreException {
                boolean isexists = false;
                int position = 0;
                DocumentSnapshot snapshot = transaction.get(drf);
                //error 트랜잭션으로 넘겨주지만 1초 이내의 동시작업은 에러를 야기하는 치명적인 단점이 존재한다. (거의 동시에 두개 이상의 댓글이 올라가면 하나만 적용되는 에러 -> 하지만 둘다 success로 표기됨)

                //읽기 작업
                int commentNum = Integer.parseInt(snapshot.getLong("comment").toString());
                ArrayList<CommentInfo> commentInfoArrayList = Get_CommentArray_From_Store(snapshot);

                for(int x=0; x<commentInfoArrayList.size(); x++){
                    if(commentInfoArrayList.get(x).getKey().equals(Key)) {
                        isexists = true;
                        position = x;
                        break;
                    }
                }

                if(isexists) {
                    //읽기작업
                    PostInfo postInfo = Get_PostInfo_From_Store(snapshot);

                    //쓰기작업
                    commentInfoArrayList.get(position).getRecomments().add(NewRecomment); //해당 대댓글 배열에 추가.
                    transaction.update(drf, "comment", commentNum + 1);
                    transaction.update(drf, "comments", commentInfoArrayList); //db에 쓰기
                    Log.d("zqwqw", "" + commentInfoArrayList.size());

                    //마무리
                    postInfo.setComment(commentNum + 1);
                    postInfo.setComments(commentInfoArrayList);

                    // Success
                    return postInfo;
                }else
                    return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<PostInfo>() {
            @Override
            public void onSuccess(PostInfo postInfo) {
                completeListener.onComplete_Set_PostInfo(postInfo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                completeListener.onFailed();
            }
        });

    }

    public void Update_Comments_With_Transaction(String docid,CommentInfo NewComment,Listener_Complete_Set_PostInfo_Transaction completeListener){

        DocumentReference drf =  db.collection(post_location).document(docid);

        db.runTransaction(new Transaction.Function<PostInfo>() {
            @Override
            public PostInfo apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(drf);
                //error 트랜잭션으로 넘겨주지만 1초 이내의 동시작업은 에러를 야기하는 치명적인 단점이 존재한다. (거의 동시에 두개 이상의 댓글이 올라가면 하나만 적용되는 에러 -> 하지만 둘다 success로 표기됨)

                //읽기 작업
                PostInfo postInfo = Get_PostInfo_From_Store(snapshot);
                ArrayList<CommentInfo> commentInfoArrayList = postInfo.getComments();
                int commentNum = postInfo.getComment();

                //쓰기작업
                commentInfoArrayList.add(NewComment);
                transaction.update(drf, "comment", commentNum + 1);
                transaction.update(drf, "comments", commentInfoArrayList);
                Log.d("zqwqw", "" + commentInfoArrayList.size());

                //마무리
                postInfo.setComment(commentNum+1);
                postInfo.setComments(commentInfoArrayList);

                // Success
                return postInfo;
            }
        }).addOnSuccessListener(new OnSuccessListener<PostInfo>() {
            @Override
            public void onSuccess(PostInfo postInfo) {
                completeListener.onComplete_Set_PostInfo(postInfo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                completeListener.onFailed();
            }
        });

    }

    public void Press_Good_Post(PostInfo postInfo, Listener_Complete_GoodPress complete_goodPress_post){ //좋아요 버튼 누르면 db의 해당 게시물의 좋아요수가 증가한다.

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //postinfo로 해당게시물에 좋아요를 누른 사람 id를 저장해주고,
        //좋아요 누른 사람이 중복으로 누르지않게 id를 찾아서 있으면 아닌거고 없으면 좋아요+1
        if(postInfo.getId().equals(user.getUid())){
            complete_goodPress_post.CannotSelf();
        }else {
            DocumentReference docref = db.collection(postInfo.getLocation()).document(postInfo.getDocid());
            //처음 누른다면
            //이후에 DB처리
            db.runTransaction(new Transaction.Function<My_Utility.Pair>() {
                @Override
                public My_Utility.Pair apply(Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(docref);

                    //읽기
                    PostInfo postInfo = Get_PostInfo_From_Store(snapshot);
                    HashMap<String, Integer> good_users = postInfo.getGood_user();

                    //이후에 백그라운드로 DB처리
                    if (good_users.containsKey(user.getUid())) //중복으로 누른다면
                    {
                        return new My_Utility.Pair(null, false);
                    } else {
                        good_users.put(user.getUid(), 1);
                        int newPopulation = postInfo.getGood();
                        newPopulation = newPopulation + 1;
                        postInfo.setGood(newPopulation);

                        //쓰기
                        transaction.update(docref, "good", newPopulation);
                        transaction.update(docref, "good_user", good_users);

                        // Success
                        return new My_Utility.Pair(postInfo, true);
                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<My_Utility.Pair>() {
                @Override
                public void onSuccess(My_Utility.Pair pair) {
                    if (pair.isSuccess())
                        complete_goodPress_post.onComplete_Good_Press((PostInfo) pair.getResult());
                    else
                        complete_goodPress_post.AlreadyDone();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    complete_goodPress_post.onFailed();
                }
            });
        }
    }

    public void Press_Good_Comment(PostInfo postInfo,int position,Listener_Complete_GoodPress complete_goodPress){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DocumentReference docref = db.collection(postInfo.getLocation()).document(postInfo.getDocid());

        if(postInfo.getComments().get(position).getId().equals(user.getUid())){ //셀프추천
            complete_goodPress.CannotSelf();
        }else {
            db.runTransaction(new Transaction.Function<My_Utility.Pair>() {
                @Override
                public My_Utility.Pair apply(Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(docref);

                    //읽기
                    PostInfo postInfo = Get_PostInfo_From_Store(snapshot);
                    ArrayList<CommentInfo> comments = postInfo.getComments();
                    CommentInfo comment = comments.get(position);

                    if (comment.getGood_user().containsKey(user.getUid())) { //중복추천
                        return new My_Utility.Pair(null, false);
                    } else {
                        comment.getGood_user().put(user.getUid(), 1);
                        comment.setGood(comment.getGood() + 1);
                        //쓰기
                        transaction.update(docref, "comments", comments);
                        // Success
                        return new My_Utility.Pair(postInfo, true); //얕은 복사로 이루어졌기때문에 따로 set하지 않아도 변경되었으리라 생각함.
                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<My_Utility.Pair>() {
                @Override
                public void onSuccess(My_Utility.Pair pair) {
                    if (pair.isSuccess())
                        complete_goodPress.onComplete_Good_Press((PostInfo) pair.getResult());
                    else
                        complete_goodPress.AlreadyDone();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    complete_goodPress.onFailed();
                }
            });
        }
    }

    public void Check_Deleted_ThePost(ArrayList<PostInfo> postList, String docid, Listener_CompletePostInfos listener_completePostInfos){

        for(int x =0; x<postList.size(); x++){ //현재 제공되어 있는 리스트에 삭제한 해당 게시물이 존재한다면 간편하게 그것만 제외하고 리셋(깔끔하고 비용이 적게든다고 생각했음)
            if(postList.get(x).getDocid().equals(docid)){
                ArrayList<PostInfo> temp;
                temp = DeepCopy_ArrayPostInfo(postList);
                temp.remove(x);
                listener_completePostInfos.onComplete_Get_PostsArrays(temp);
                break;
            }
        }
    }

    public ArrayList<PostInfo> DeepCopy_ArrayPostInfo(ArrayList<PostInfo> oldone){

        ArrayList<PostInfo> newone = new ArrayList<>();

        for(int x=0; x<oldone.size(); x++) {
            if(oldone.get(x)==null)
                continue;
            newone.add(new PostInfo(oldone.get(x)));
        }

        return newone;
    }

    public ArrayList<CommentInfo> DeepCopy_CommentInfo(ArrayList<CommentInfo> oldone){

        ArrayList<CommentInfo> newone = new ArrayList<>();

        for(int x=0; x<oldone.size(); x++) {
            if(oldone.get(x)==null)
                continue;
            newone.add(new CommentInfo(oldone.get(x)));
        }
        return newone;
    }

    public ArrayList<RecommentInfo> deepCopy_RecommentInfo(ArrayList<RecommentInfo> oldone){

        ArrayList<RecommentInfo> newone = new ArrayList<>();

        for(int x=0; x<oldone.size(); x++) {
            if(oldone.get(x)==null)
                continue;
            newone.add(new RecommentInfo(oldone.get(x)));
        }
        return newone;
    }

    public ArrayList<CommentInfo> Get_CommentArray_From_Store(DocumentSnapshot document){

        ArrayList<CommentInfo> commentInfoArrayList = new ArrayList<>();

        if(((ArrayList<HashMap<String,Object>>) document.getData().get("comments")).size() != 0){
            for(int x=0; x<((ArrayList<HashMap<String,Object>>) document.getData().get("comments")).size(); x++) {

                HashMap<String, Object> commentsmap = ((ArrayList<HashMap<String, Object>>) document.getData().get("comments")).get(x);
                /////////////////////////////////////////////////////////////////////////////
                Object bring = (Object)commentsmap.get("good_user");
                HashMap<String,Integer> goodusers = new HashMap<>( (Map<? extends String, ? extends Integer>) bring);

                for(Map.Entry e : goodusers.entrySet())
                    Log.d("Postcontrol",e.getKey()+", "+e.getValue());

                CommentInfo commentInfo = new CommentInfo((String) commentsmap.get("contents"), (String) commentsmap.get("publisher"),
                        ((Timestamp)commentsmap.get("createdAt")).toDate(),
                        (String) commentsmap.get("id"),
                        ((Long)(commentsmap.get("good"))).intValue(),
                        goodusers,
                        Get_RecommentArray_From_CommentsMap(commentsmap),
                        (String) commentsmap.get("key")
                );

                commentInfoArrayList.add(commentInfo);

            }
        }

        return commentInfoArrayList;
    }

    public ArrayList<RecommentInfo> Get_RecommentArray_From_CommentsMap(HashMap<String, Object> commentsmap ){
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

    public PostInfo Get_PostInfo_From_Store(DocumentSnapshot documentSnapshot){
        ArrayList<CommentInfo> commentInfoArrayList = Get_CommentArray_From_Store(documentSnapshot);

        PostInfo postInfo = new PostInfo(
                documentSnapshot.get("id").toString(),
                documentSnapshot.get("publisher").toString(),
                documentSnapshot.get("title").toString(),
                documentSnapshot.get("contents").toString(),
                (ArrayList<String>) documentSnapshot.getData().get("formats"),
                new Date(documentSnapshot.getDate("createdAt").getTime()),
                documentSnapshot.getId(),
                Integer.parseInt(documentSnapshot.get("good").toString()), Integer.parseInt(documentSnapshot.get("comment").toString()), post_location,
                (ArrayList<String>) documentSnapshot.getData().get("storagepath"), commentInfoArrayList,
                (HashMap<String, Integer>) documentSnapshot.getData().get("good_user")
        );

        return postInfo;
    }


}
