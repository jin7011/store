package com.example.sns_project.CustomLibrary;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.LetterInfo;
import com.example.sns_project.info.NotificationInfo;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.example.sns_project.util.Named.ALREADY_DONE;
import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.NOT_EXIST;
import static com.example.sns_project.util.Named.SEARCH_LIMIT;
import static com.example.sns_project.util.Named.SEC;
import static com.example.sns_project.util.Named.SUCCESS;
import static com.example.sns_project.util.Named.UPLOAD_LIMIT;
import static com.example.sns_project.util.Named.WRITE_RESULT;

/**
 * use this with my_utility for requesting posts
 * in recycler_init() after using my utility or in same time
 */

public final class PostControler {

    private String post_location;
    private final FirebaseFirestore Store = FirebaseFirestore.getInstance();
    private final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//    public static FirebaseUser FIREBASE_USER = FirebaseAuth.getInstance().getCurrentUser();
    private My_Utility my_utility;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

    public PostControler() {
    }

    public PostControler(String post_location, My_Utility my_utility) {
        this.post_location = post_location;
        this.my_utility = my_utility;
        this.recyclerView = my_utility.getRecyclerView();
        this.adapter = my_utility.getAdapter();
    }

    public PostControler(String post_location) {
        this.post_location = post_location;
    }

    public interface Listener_Complete_Get_PostInfo {
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

    public interface Listener_Complete_Get_Letters {
        void onComplete_Get_Letters(ArrayList<LetterInfo> Letters);
    }

    public interface Test {
        void onComplete_Get_Letters(ArrayList<LetterInfo> Letters);
        void NewLetters(LetterInfo letterInfo);
    }

    public interface Listener_Check_Room {
        void Done();
    }

    public interface Listener_Room {
        void onAdded(ChatRoomInfo room);
        void onModified(ChatRoomInfo room);
        void onDeleted(ChatRoomInfo room);
    }

    public interface Listener_Noti {
        void onAdded(NotificationInfo noti);
        void onModified(NotificationInfo noti);
        void onDeleted(NotificationInfo noti);
    }

    public interface Listener_UpLoadPost {
        void onComplete();
        void onFail();
    }

    public interface Listener_Delete_Noti{
        void onComplete();
    }

    public void Search_Post(ArrayList<PostInfo> Loaded_Posts, String KeyWord, Listener_CompletePostInfos listener_completePostInfos) {

        ArrayList<PostInfo> newPosts = new ArrayList<>();
        ArrayList<PostInfo> temp = DeepCopy_ArrayPostInfo(Loaded_Posts);

        Date OldestDate = Loaded_Posts.size() == 0 ? new Date() : Loaded_Posts.get(Loaded_Posts.size() - 1).getCreatedAt();

        Log.d("zozozozozo", "시작: " + newPosts.size());

        Store.collection(post_location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", OldestDate)//업스크롤 효과 (위에서부터 최신상태로)
                .limit(SEARCH_LIMIT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if (document.exists()) {
                                    String nickname = document.get("publisher").toString();
                                    String content = document.get("contents").toString();
                                    String title = document.get("title").toString();

                                    if (nickname.contains(KeyWord) || content.contains(KeyWord) || title.contains(KeyWord)) {

                                        Log.d("가져옴", document.getId() + " => " + document.getData());
                                        newPosts.add(new PostInfo(Get_PostInfo_From_Doc(document)));
                                        Log.d("zozozozozo", "찾은거: " + title);
                                    }
                                }

                            }///////////////////////////////////////////////////////////////////////완료
                            Log.d("zozozozozo", "끝: " + newPosts.size());
                            temp.addAll(newPosts);
                            listener_completePostInfos.onComplete_Get_PostsArrays(temp);
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void Request_NewPosts(Listener_CompletePostInfos listener_completePostInfos) {

        //최신글 20개를 가져옴.
        Date NewDate = new Date();
        ArrayList<PostInfo> newPosts = new ArrayList<>();
        Log.d("zozozozozo", "시작: " + newPosts.size());

        Store.collection(post_location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", NewDate)//업스크롤 효과 (위에서부터 최신상태로)
                .limit(UPLOAD_LIMIT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if (document.exists()) {
                                    Log.d("가져옴", document.getId() + " => " + document.getData());
                                    newPosts.add(new PostInfo(Get_PostInfo_From_Doc(document)));
                                }

                            }///////////////////////////////////////////////////////////////////////완료
                            Log.d("zozozozozo", "끝: " + newPosts.size());
                            listener_completePostInfos.onComplete_Get_PostsArrays(newPosts);
                        } else {
                            Log.d("실패함", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    public void Request_Posts(ArrayList<PostInfo> Loaded_Posts, Listener_CompletePostInfos listener_completePostInfos) {

        //시간을 기점으로 이후의 게시물 20개를 가져옴 (NextPosts라고 하려다가 New랑 헷갈려서 After로 바꿈)
        ArrayList<PostInfo> newPosts = new ArrayList<>();
        ArrayList<PostInfo> temp = DeepCopy_ArrayPostInfo(Loaded_Posts);

        Date OldestDate = Loaded_Posts.size() == 0 ? new Date() : Loaded_Posts.get(Loaded_Posts.size() - 1).getCreatedAt();

        Log.d("zozozozozo", "시작: " + newPosts.size());

        Store.collection(post_location)
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", OldestDate)//업스크롤 효과 (위에서부터 최신상태로)
                .limit(UPLOAD_LIMIT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Log.d("가져옴", document.getId() + " => " + document.getData());
                                newPosts.add(new PostInfo(Get_PostInfo_From_Doc(document)));

                            }///////////////////////////////////////////////////////////////////////완료
                            Log.d("zozozozozo", "끝: " + newPosts.size());
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

        if (flag) { //해당 게시물이 로드되어있던 거라면, 그것만 갱신
            final PostInfo[] newpostInfo = new PostInfo[1];
            int finalIdx = idx; //위 2줄은 비동기랑 맞추려고 어쩔수없이

            Store.collection(post_location).document(docid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            newpostInfo[0] = Get_PostInfo_From_Doc(document);
                            temp.remove(finalIdx);
                            temp.add(finalIdx, newpostInfo[0]);
                            listener_completePostInfos.onComplete_Get_PostsArrays(temp);
                        }
                    }

                }
            });
        }
    }

    public void Get_UniPost(String docid, Listener_Complete_Get_PostInfo listener_complete_get_postInfo) {

        Store.collection(post_location).document(docid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    PostInfo postInfo = Get_PostInfo_From_Doc(documentSnapshot);
                    listener_complete_get_postInfo.onComplete_Get_PostInfo(postInfo);
                } else {
                    my_utility.Toast("존재하지 않는 게시물입니다.");
                }
            }
        });
    }

    public void Set_UniPost(PostInfo postInfo, Listener_Complete_Set_PostInfo listener_complete_set_postInfo) {

        Store.collection(post_location).document(postInfo.getDocid()).update(postInfo.getPostInfo()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                listener_complete_set_postInfo.onComplete_Set_PostInfo();
            }
        });
    }

    public void Update_ReComments_With_Transaction(String docid, String Key, RecommentInfo NewRecomment, Listener_Complete_Set_PostInfo_Transaction completeListener) {

        DocumentReference drf = Store.collection(post_location).document(docid);

        Store.runTransaction(new Transaction.Function<PostInfo>() {
            @Override
            public PostInfo apply(Transaction transaction) throws FirebaseFirestoreException {
                boolean isexists = false;
                int position = 0;
                DocumentSnapshot snapshot = transaction.get(drf);
                //error 트랜잭션으로 넘겨주지만 1초 이내의 동시작업은 에러를 야기하는 치명적인 단점이 존재한다. (거의 동시에 두개 이상의 댓글이 올라가면 하나만 적용되는 에러 -> 하지만 둘다 success로 표기됨)

                //읽기 작업
                int commentNum = Integer.parseInt(snapshot.getLong("comment").toString());
                ArrayList<CommentInfo> commentInfoArrayList = Get_CommentArray_From_Store(snapshot);

                for (int x = 0; x < commentInfoArrayList.size(); x++) {
                    if (commentInfoArrayList.get(x).getKey().equals(Key)) {
                        isexists = true;
                        position = x;
                        break;
                    }
                }

                if (isexists) {
                    //읽기작업
                    PostInfo postInfo = Get_PostInfo_From_Doc(snapshot);

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
                } else
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

    public void Update_Comments_With_Transaction(String docid, CommentInfo NewComment, Listener_Complete_Set_PostInfo_Transaction completeListener) {

        DocumentReference drf = Store.collection(post_location).document(docid);

        Store.runTransaction(new Transaction.Function<PostInfo>() {
            @Override
            public PostInfo apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(drf);
                //error 트랜잭션으로 넘겨주지만 1초 이내의 동시작업은 에러를 야기하는 치명적인 단점이 존재한다. (거의 동시에 두개 이상의 댓글이 올라가면 하나만 적용되는 에러 -> 하지만 둘다 success로 표기됨)

                //읽기 작업
                PostInfo postInfo = Get_PostInfo_From_Doc(snapshot);
                ArrayList<CommentInfo> commentInfoArrayList = postInfo.getComments();
                int commentNum = postInfo.getComment();

                //쓰기작업
                commentInfoArrayList.add(NewComment);
                transaction.update(drf, "comment", commentNum + 1);
                transaction.update(drf, "comments", commentInfoArrayList);
                Log.d("zqwqw", "" + commentInfoArrayList.size());

                //마무리
                postInfo.setComment(commentNum + 1);
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

    public void Press_Good_Post(PostInfo postInfo, Listener_Complete_GoodPress complete_goodPress) { //좋아요 버튼 누르면 db의 해당 게시물의 좋아요수가 증가한다.

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //postinfo로 해당게시물에 좋아요를 누른 사람 id를 저장해주고,
        //좋아요 누른 사람이 중복으로 누르지않게 id를 찾아서 있으면 아닌거고 없으면 좋아요+1
        if (postInfo.getId().equals(user.getUid())) {
            complete_goodPress.CannotSelf();
        } else {
            DocumentReference docref = Store.collection(postInfo.getLocation()).document(postInfo.getDocid());
            //처음 누른다면
            //이후에 DB처리
            Store.runTransaction(new Transaction.Function<My_Utility.Pair>() {
                @Override
                public My_Utility.Pair apply(Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(docref);

                    if (snapshot.exists()) {
                        //읽기
                        PostInfo postInfo = Get_PostInfo_From_Doc(snapshot);
                        HashMap<String, Integer> good_users = postInfo.getGood_user();

                        //이후에 백그라운드로 DB처리
                        if (good_users.containsKey(user.getUid())) //중복으로 누른다면
                        {
                            return new My_Utility.Pair(null, ALREADY_DONE);
                        } else {
                            good_users.put(user.getUid(), 1);
                            int newPopulation = postInfo.getGood();
                            newPopulation = newPopulation + 1;
                            postInfo.setGood(newPopulation);

                            //쓰기
                            transaction.update(docref, "good", newPopulation);
                            transaction.update(docref, "good_user", good_users);

                            // Success
                            return new My_Utility.Pair(postInfo, SUCCESS);
                        }
                    } else
                        return new My_Utility.Pair(null, NOT_EXIST);
                }
            }).addOnSuccessListener(new OnSuccessListener<My_Utility.Pair>() {
                @Override
                public void onSuccess(My_Utility.Pair pair) {
                    if (pair.getIsSuccess() == SUCCESS)
                        complete_goodPress.onComplete_Good_Press((PostInfo) pair.getResult());
                    else if (pair.getIsSuccess() == ALREADY_DONE)
                        complete_goodPress.AlreadyDone();
                    else if (pair.getIsSuccess() == NOT_EXIST)
                        complete_goodPress.onFailed();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    complete_goodPress.onFailed();
                }
            });
        }
    }

    public void Press_Good_Comment(PostInfo postInfo, int position, Listener_Complete_GoodPress complete_goodPress) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docref = Store.collection(postInfo.getLocation()).document(postInfo.getDocid());
        String Key = postInfo.getComments().get(position).getKey();

        if (postInfo.getComments().get(position).getId().equals(user.getUid())) { //셀프추천
            complete_goodPress.CannotSelf();
        } else {
            Store.runTransaction(new Transaction.Function<My_Utility.Pair>() {
                @Override
                public My_Utility.Pair apply(Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(docref);

                    if (snapshot.exists()) {
                        //읽기
                        PostInfo NewPostInfo = Get_PostInfo_From_Doc(snapshot);
                        ArrayList<CommentInfo> comments = NewPostInfo.getComments();
                        CommentInfo comment = comments.get(position);
                        int CommentIDX = 0;
                        CommentIDX = Find_Comment(comments, Key);

                        if (CommentIDX != -1) //존재한다면,
                        {
                            if (comment.getGood_user().containsKey(user.getUid())) { //중복추천
                                return new My_Utility.Pair(null, ALREADY_DONE);
                            } else {
                                comment.getGood_user().put(user.getUid(), 1);
                                comment.setGood(comment.getGood() + 1);
                                //쓰기
                                transaction.update(docref, "comments", comments);
                                // Success
                                return new My_Utility.Pair(NewPostInfo, SUCCESS); //얕은 복사로 이루어졌기때문에 따로 set하지 않아도 변경되었으리라 생각함.
                            }
                        } else //댓글이 존재하지 않는다면
                            return new My_Utility.Pair(null, NOT_EXIST);
                    } else//포스트가 존재하지 않는다면
                        return new My_Utility.Pair(null, NOT_EXIST);
                }
            }).addOnSuccessListener(new OnSuccessListener<My_Utility.Pair>() {
                @Override
                public void onSuccess(My_Utility.Pair pair) {
                    if (pair.getIsSuccess() == SUCCESS)
                        complete_goodPress.onComplete_Good_Press((PostInfo) pair.getResult());
                    else if (pair.getIsSuccess() == ALREADY_DONE)
                        complete_goodPress.AlreadyDone();
                    else if (pair.getIsSuccess() == NOT_EXIST)
                        complete_goodPress.onFailed();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    complete_goodPress.onFailed();
                }
            });
        }
    }

    public void Press_Good_ReComment(PostInfo postInfo, CommentInfo Parent_Comment, RecommentInfo recomment, Listener_Complete_GoodPress complete_goodPress) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docref = Store.collection(postInfo.getLocation()).document(postInfo.getDocid());

        if (recomment.getId().equals(user.getUid())) { //셀프추천
            complete_goodPress.CannotSelf();
        } else {
            Store.runTransaction(new Transaction.Function<My_Utility.Pair>() {
                @Override
                public My_Utility.Pair apply(Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(docref);
                    if (snapshot.exists()) {
                        int CommentIDX = 0;
                        int RecommentIDX = 0;
                        //읽기
                        PostInfo NewPostInfo = Get_PostInfo_From_Doc(snapshot);
                        ArrayList<CommentInfo> comments = NewPostInfo.getComments();
                        My_Utility.IDX_Pair IDX_pair = Find_Recomments_From_Parent_Comment(NewPostInfo.getComments(), recomment, Parent_Comment.getKey());

                        if (IDX_pair != null) {//가져왔을때 댓글이 존재해야함
                            CommentIDX = IDX_pair.getResult_1();
                            RecommentIDX = IDX_pair.getResult_2();
                            Log.d("cddaa", "" + CommentIDX + "," + RecommentIDX);

                            //둘다 존재하고, 찾았다면
                            RecommentInfo NewRecomment = comments.get(CommentIDX).getRecomments().get(RecommentIDX);
                            HashMap<String, Integer> good_user = NewRecomment.getGood_user();

                            if (good_user.containsKey(user.getUid())) { // 중복추천
                                Log.d("wndqhr", "중복추천");
                                Log.d("wndqhr", "중복추천");
                                Log.d("wndqhr", "중복추천");
                                Log.d("wndqhr", "중복추천");
                                Log.d("wndqhr", "중복추천");
                                return new My_Utility.Pair(null, ALREADY_DONE);
                            } else {
                                good_user.put(user.getUid(), 1);
                                NewRecomment.setGood(NewRecomment.getGood() + 1);
                                //쓰기
                                transaction.update(docref, "comments", comments);
                                // Success
                                return new My_Utility.Pair(NewPostInfo, SUCCESS);
                            }
                        } else //댓글이 삭제되었다면,
                        {
                            Log.d("wndqhr", "삭제");
                            Log.d("wndqhr", "삭제");
                            Log.d("wndqhr", "삭제");
                            Log.d("wndqhr", "삭제");
                            Log.d("wndqhr", "삭제");
                            return new My_Utility.Pair(null, NOT_EXIST);
                        }
                    } else //포스트가 존재하지 않는다면
                    {
                        Log.d("wndqhr", "shwhswo");
                        Log.d("wndqhr", "shwhswo");
                        Log.d("wndqhr", "shwhswo");
                        Log.d("wndqhr", "shwhswo");
                        Log.d("wndqhr", "shwhswo");
                        Log.d("wndqhr", "shwhswo");
                        Log.d("wndqhr", "shwhswo");
                        Log.d("wndqhr", "shwhswo");
                        return new My_Utility.Pair(null, NOT_EXIST);

                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<My_Utility.Pair>() {
                @Override
                public void onSuccess(My_Utility.Pair pair) {
                    if (pair.getIsSuccess() == SUCCESS)
                        complete_goodPress.onComplete_Good_Press((PostInfo) pair.getResult());
                    else if (pair.getIsSuccess() == ALREADY_DONE)
                        complete_goodPress.AlreadyDone();
                    else if (pair.getIsSuccess() == NOT_EXIST)
                        complete_goodPress.onFailed();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    complete_goodPress.onFailed();
                }
            });
        }
    }

    public void Set_Listener_Room(String id,Listener_Room get_roomKeys) {

        Store.collection("USER").document(id).collection("Rooms")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                    if(dc.getType() == DocumentChange.Type.ADDED){
                        if(dc.getDocument().toObject(ChatRoomInfo.class).getLatestMessage() != null) {
                            get_roomKeys.onAdded(dc.getDocument().toObject(ChatRoomInfo.class));
                            Log.d("clzl12", "added: " + dc.getDocument().toObject(ChatRoomInfo.class).getLatestMessage() + "");
                        }
                    }
                    if(dc.getType() == DocumentChange.Type.MODIFIED){
                        if(dc.getDocument().toObject(ChatRoomInfo.class).getLatestMessage() != null) {
                            get_roomKeys.onModified(dc.getDocument().toObject(ChatRoomInfo.class));
                            Log.d("clzl12", "modified: " + dc.getDocument().toObject(ChatRoomInfo.class).getLatestMessage() + "");
                        }
                    }
                    if(dc.getType() == DocumentChange.Type.REMOVED){
                        if(dc.getDocument().toObject(ChatRoomInfo.class).getLatestMessage() != null) {
                            get_roomKeys.onDeleted(dc.getDocument().toObject(ChatRoomInfo.class));
                            Log.d("clzl12", "modified: " + dc.getDocument().toObject(ChatRoomInfo.class).getLatestMessage() + "");
                        }
                    }
                }
            }
        });
    }

    public void Set_Listener_Noti(String id,Listener_Noti listener_noti){
        Store.collection("USER").document(id).collection("Notification").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange dc : value.getDocumentChanges()) {

                    NotificationInfo noti = dc.getDocument().toObject(NotificationInfo.class);

                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        if (noti.getDocid() != null) {
                            listener_noti.onAdded(noti);
                            Log.d("clzl12", "added: " + noti.getType() + "");
                        }
                    }
                    if (dc.getType() == DocumentChange.Type.MODIFIED) {
                        if (noti.getDocid() != null) {
                            listener_noti.onModified(noti);
                            Log.d("clzl12", "modified: " +noti.getType() + "");
                        }
                    }
                    if (dc.getType() == DocumentChange.Type.REMOVED) {
                        if (noti.getDocid() != null) {
                            listener_noti.onDeleted(noti);
                            Log.d("clzl12", "modified: " + noti.getType() + "");
                        }
                    }
                }
            }
        });
    }

    public void Delete_Noti(String id,String docid,Listener_Delete_Noti listener_delete_noti){
        Store.collection("USER").document(id).collection("Notification").document(docid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener_delete_noti.onComplete();
            }
        });
    }

    public void Update_letter(String Key, String my_id,String my_token,String other_id,String other_token,ChatRoomInfo NewRoom,LetterInfo NewLetter) {
        Log.d("czo123","시작은했냐??");

        Store.collection("USER").document(other_id).collection("Rooms").document(Key).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){ //상대방에게 방이 존재한다면 (나간적이 없다면) 정상적으로 카운트
                        Log.d("czo123","이그지스트");
                        Set_Latest_and_Count(Key,my_id,my_token,other_id,other_token,NewLetter.getContents(),NewLetter.getCreatedAt());

                        Store.collection("USER").document(my_id).collection("Rooms").document(Key).collection("Letters").add(NewLetter);
                        Store.collection("USER").document(other_id).collection("Rooms").document(Key).collection("Letters").add(NewLetter);
                    }else{
                        //상대방이 나갔던 적이 있어서 방이 존재하지 않는다면, 상대방에게 새롭게 방을 만들어줌. -> 그 다음 정상카운트
                        Store.collection("USER").document(other_id).collection("Rooms").document(Key).set(NewRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Set_Latest_and_Count(Key,my_id,my_token,other_id,other_token,NewLetter.getContents(),NewLetter.getCreatedAt());
                                Log.d("czo123","존재x");
                                Store.collection("USER").document(my_id).collection("Rooms").document(Key).collection("Letters").add(NewLetter);
                                Store.collection("USER").document(other_id).collection("Rooms").document(Key).collection("Letters").add(NewLetter);
                            }
                        });
                    }
                }
            }
        });

    }

    public void Create_NewRoom(String my_token,String Key, String my_nick, String my_id,String other_token,String other_nick, String other_id, Listener_Check_Room listener_Check_room_) {

        Store.collection("USER").document(my_id).collection("Rooms").document(Key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) { //이미 있는 방이라면, 방에서 나간적이 있는지 체크해서 시간을 넘겨라
                    listener_Check_room_.Done();
                } else { //새로만들어야 한다면,
                    ChatRoomInfo Room = new ChatRoomInfo(my_nick, my_id, new Date().getTime(), 0,my_token, other_nick, other_id, new Date().getTime(), 0,other_token, Key,false);
                    Store.collection("USER").document(my_id).collection("Rooms").document(Key).set(Room);
                    listener_Check_room_.Done();
                }
            }
        });
    }

    public void Set_Before_Exit(String Key, String my_id, String latestMessage, Long latestDate) {

        Map<String, Object> map = new HashMap<>();

        DocumentReference doc = Store.collection("USER").document(my_id).collection("Rooms").document(Key);

        Store.runTransaction(new Transaction.Function<Object>() {
            @Nullable
            @Override
            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot snapshot = transaction.get(doc);

                if (snapshot.exists()) {
                    //읽기
                    ChatRoomInfo room = snapshot.toObject(ChatRoomInfo.class);

                    if (room.getUser1_id().equals(my_id)) {
                        map.put("user1_count",0);
                        map.put("latestMessage", latestMessage);
                        map.put("latestDate", latestDate);
                        transaction.update(doc, map);
                    } else {
                        map.put("user2_count",0);
                        map.put("latestMessage", latestMessage);
                        map.put("latestDate", latestDate);
                        transaction.update(doc, map);
                    }
                }
                return null;
            }
        });
    }

    public void Set_Latest_and_Count(String Key, String my_id,String my_token,String others_id,String other_token, String latestMessage, Long latestDate) {

        Map<String, Object> map = new HashMap<>();

        DocumentReference doc = Store.collection("USER").document(my_id).collection("Rooms").document(Key);

        Store.runTransaction(new Transaction.Function<Object>() {
            @Nullable
            @Override
            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot snapshot = transaction.get(doc);

                if (snapshot.exists()) {
                    //읽기
                    ChatRoomInfo room = snapshot.toObject(ChatRoomInfo.class);

                    if (room.getUser1_id().equals(my_id)) {
                        map.put("user2_count", room.getUser2_count() + 1);
                        map.put("latestMessage", latestMessage);
                        map.put("latestDate", latestDate);
                        map.put("token",room.getUser2_token());
                        map.put("isNew",true);
                        map.put("sender",room.getUser1());
                        Log.d("zxkxk23","1qjs: "+room.getUser2_token());
                        transaction.update(doc, map);
                    } else {
                        map.put("user1_count", room.getUser1_count() + 1);
                        map.put("latestMessage", latestMessage);
                        map.put("latestDate", latestDate);
                        map.put("token",room.getUser1_token());
                        map.put("isNew",true);
                        map.put("sender",room.getUser2());
                        Log.d("zxkxk23","2qjs: "+room.getUser1_token());
                        transaction.update(doc, map);
                    }
                }
                return null;
            }
        });

        DocumentReference doc2 = Store.collection("USER").document(others_id).collection("Rooms").document(Key);

        Store.runTransaction(new Transaction.Function<Object>() {
            @Nullable
            @Override
            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot snapshot = transaction.get(doc2);

                if (snapshot.exists()) {
                    //읽기
                    ChatRoomInfo room = snapshot.toObject(ChatRoomInfo.class);
                    if (room.getUser1_id().equals(my_id)) {
                        map.put("user2_count", room.getUser2_count() + 1);
                        map.put("latestMessage", latestMessage);
                        map.put("latestDate", latestDate);
                        map.put("token",room.getUser2_token());
                        map.put("isNew",true);
                        map.put("sender",room.getUser1());
                        transaction.update(doc2, map);
                    } else {
                        map.put("user1_count", room.getUser1_count() + 1);
                        map.put("latestMessage", latestMessage);
                        map.put("latestDate", latestDate);
                        map.put("token",room.getUser1_token());
                        map.put("isNew",true);
                        map.put("sender",room.getUser2());
                        transaction.update(doc2, map);
                    }
                }
                return null;
            }
        });

    }

    public void Set_Count_Zero(String Key, String my_id) {

        Store.collection("USER").document(my_id).collection("Rooms").document(Key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) { //이미 있는 방이라면,

                    ChatRoomInfo room = documentSnapshot.toObject(ChatRoomInfo.class);

                    if (room.getUser1_id().equals(my_id)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("user1_count", 0);
                        Store.collection("Rooms").document(Key).update(map);
                    } else {
                        Map<String, Object> map = new HashMap<>();
                        map.put("user2_count", 0);
                        Store.collection("Rooms").document(Key).update(map);
                    }
                }
            }
        });
    }

    public void Delete_Room(String Key, String my_id){

        Store.collection("USER").document(my_id).collection("Rooms").document(Key).collection("Letters")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(int x=0; x< task.getResult().getDocuments().size(); x++)
                    task.getResult().getDocuments().get(x).getReference().delete();
            }
        });

        Store.collection("USER").document(my_id).collection("Rooms").document(Key).delete();
    }

    public void Set_Listener_Letters(String Key, String my_id, long latest_date, Test test){

        ArrayList<LetterInfo> letters = new ArrayList<>();

        Store.collection("USER").document(my_id).collection("Rooms").document(Key).collection("Letters")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot snapshot : task.getResult())
                                    letters.add(0,snapshot.toObject(LetterInfo.class));
                                Log.d("ajdua",""+letters.size());
                                test.onComplete_Get_Letters(letters);
                            }
                        }
                    }
                });

        Store.collection("USER").document(my_id).collection("Rooms").document(Key).collection("Letters")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .whereGreaterThanOrEqualTo("createdAt",latest_date)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) { //추가된 채팅방
                                test.NewLetters(dc.getDocument().toObject(LetterInfo.class));
                            }
                        }
                    }
                });

    }

    public void Bring_Letters(String Key, String my_id,long latest_date, Listener_Complete_Get_Letters complete_get_letters){

        ArrayList<LetterInfo> letters = new ArrayList<>();

        Store.collection("USER").document(my_id).collection("Rooms").document(Key).collection("Letters")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .whereLessThan("createdAt",latest_date)
                .limit(UPLOAD_LIMIT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("asdaszzzcq22",letters.size()+"");
                                letters.add(0,document.toObject(LetterInfo.class));
                            }
                            complete_get_letters.onComplete_Get_Letters(letters);
                        }
                    }
                });
    }

    public void Bring_MyPosts(String my_id,Listener_CompletePostInfos listener_completePostInfos){

        Store.collection("USER").document(my_id).collection("MyPosts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<My_Utility.Pair> pair = new ArrayList<>();
                    ArrayList<PostInfo> posts = new ArrayList<>();

                    for(Iterator<DocumentSnapshot> iter = Objects.requireNonNull(task.getResult()).getDocuments().iterator(); iter.hasNext(); ) {
                        DocumentSnapshot doc = iter.next();
                        String location = doc.getString("location");
                        String Docid =  doc.getString("Docid");
                        Log.d("zoz23",location+", "+Docid);
                        pair.add(new My_Utility.Pair(location,Docid));
                    }

                    //firebase에서 한번에 데이터를 긁어올 수가 없는 구조로 되어 있어서(뷰,쿼리 지원x) 여기저기서 긁어오느라 rx자바도 못쓰고 어쩔 수 없이 재귀로..
                    Bring_MyPosts_byRecursive(0, pair, posts, new Listener_CompletePostInfos() {
                        @Override
                        public void onComplete_Get_PostsArrays(ArrayList<PostInfo> NewPostInfos) {
                            Log.d("zoz23","result: "+posts.size());
                            listener_completePostInfos.onComplete_Get_PostsArrays(posts);
                        }
                    });
                }
            }
        });
    }

    public void Bring_MyPosts_byRecursive(int cnt,ArrayList<My_Utility.Pair> pair, ArrayList<PostInfo> posts,Listener_CompletePostInfos listener_completePostInfos){

        if(pair.size() == cnt) {
            listener_completePostInfos.onComplete_Get_PostsArrays(posts);
            return;
        }

        String location = pair.get(cnt).getLocation();
        String Docid =  pair.get(cnt).getDocid();

        Store.collection(location).document(Docid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                PostInfo postInfo = Objects.requireNonNull(task.getResult()).toObject(PostInfo.class);
                posts.add(postInfo);
                Log.d("zoz23","size: "+posts.size());
                Log.d("zoz23","size: "+postInfo);
                Bring_MyPosts_byRecursive(cnt+1,pair,posts,listener_completePostInfos);
            }
        });
    }

    public int Find_Comment(ArrayList<CommentInfo> comments, String Key){
        for(int x=0; x<comments.size(); x++){
            if(comments.get(x).getKey().equals(Key))
                return x;
        }
        return -1;
    }

    public My_Utility.IDX_Pair Find_Recomments_From_Parent_Comment(ArrayList<CommentInfo> comments, RecommentInfo recommentInfo, String Key){

        for(int C_IDX=0; C_IDX<comments.size(); C_IDX++){
            if(comments.get(C_IDX).getKey().equals(Key)){
                //해당 댓글의 위치를 찾아서 대댓글 배열을 만들어준다.
                ArrayList<RecommentInfo> ReComments = comments.get(C_IDX).getRecomments();
                Log.d("cddaaxz",""+C_IDX);
                for(int R_IDX=0; R_IDX<ReComments.size();R_IDX++){
                    //해당 대댓글의 위치를 찾았다면 반환한다.
                    if(ReComments.get(R_IDX).getCreatedAt().getTime() == recommentInfo.getCreatedAt().getTime()){
                        return new My_Utility.IDX_Pair(C_IDX,R_IDX);
                    }
                }
            }
        }
        return null;
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

    public void Upload_Post_Store(DocumentReference documentReference, final PostInfo postInfo, Listener_UpLoadPost listener_upLoadPost) {
        documentReference.set(postInfo.getPostInfo())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String,String> map = new HashMap<>();
                        map.put("Docid",postInfo.getDocid());
                        map.put("location",postInfo.getLocation());
                        Store.collection("USER").document(postInfo.getId()).collection("MyPosts").document(postInfo.getDocid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                listener_upLoadPost.onComplete();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                listener_upLoadPost.onFail();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      listener_upLoadPost.onFail();
                    }
                });
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

            Object bring = (Object)recommentsmap.get("good_user");
            HashMap<String,Integer> goodusers = new HashMap<>( (Map<? extends String, ? extends Integer>) bring);

            RecommentInfo recommentInfo = new RecommentInfo(
                    (String)recommentsmap.get("contents"),
                    (String)recommentsmap.get("publisher"),
                    ((Timestamp)recommentsmap.get("createdAt")).toDate(),
                    (String)recommentsmap.get("id"),
                    ((Long)(recommentsmap.get("good"))).intValue(),
                    goodusers
            );

            recommentInfoArrayList.add(recommentInfo);
        }

        return recommentInfoArrayList;
    }

    public PostInfo Get_PostInfo_From_Doc(DocumentSnapshot documentSnapshot){
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

    public ArrayList<PostInfo> DeepCopy_ArrayPostInfo(ArrayList<PostInfo> oldone){

        ArrayList<PostInfo> newone = new ArrayList<>();

        for(int x=0; x<oldone.size(); x++) {
            if(oldone.get(x)==null)
                continue;
            newone.add(new PostInfo(oldone.get(x)));
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

    @SuppressLint({"DefaultLocale", "SimpleDateFormat"})
    public static String MessageTime_to_String(long createdAt , Date nowDate){

        long ctime = nowDate.getTime();
//        long regTime = postdate.getTime();

        long diffTime = Math.abs(ctime - createdAt) / 1000;
        String msg;
//        Log.d("acdksld",diffTime+" d");
//        Log.d("acdksld",ctime+" now");
//        Log.d("acdksld",createdAt+" createat");
//        Log.d("acdksld",(ctime - createdAt)+" 차이");

        if (diffTime < SEC) {
            msg = "방금 전";
        } else if ((diffTime /= SEC) < MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= MIN) < HOUR) {
            msg = new SimpleDateFormat("HH:mm").format(new Date(createdAt));
            int hour = (Character.getNumericValue(msg.charAt(0))*10 + Character.getNumericValue(msg.charAt(1)));
            int min = (Character.getNumericValue(msg.charAt(3))*10 + Character.getNumericValue(msg.charAt(4)));
            if(hour > 12) {
                msg = "오후 "+ (hour-12) + ":" + String.format("%02d", min);
            }else
                msg = "오전 " + msg;
//        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
//            msg = (diffTime) + "일 전";
//        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
//            msg = (diffTime) + "달 전";
        } else {
            msg = new SimpleDateFormat("yyyy년MM월dd일").format(new Date(createdAt));
        }
        return msg;
    }

    @SuppressLint("SimpleDateFormat")
    public static String Time_to_String(Date postdate, Date nowDate){

        long ctime = nowDate.getTime();
        long regTime = postdate.getTime();

        long diffTime =  Math.abs(ctime - regTime)/ 1000;
        String msg = null;

        if (diffTime < SEC) {
            msg = "방금 전";
        } else if ((diffTime /= SEC) < MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= MIN) < HOUR) {
            msg = new SimpleDateFormat("HH:mm").format(postdate);
//        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
//            msg = (diffTime) + "일 전";
//        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
//            msg = (diffTime) + "달 전";
        } else {
            msg = new SimpleDateFormat("yyyy년MM월dd일").format(postdate);
        }
        return msg;
    }

}
