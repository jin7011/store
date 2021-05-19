package com.example.sns_project.CustomLibrary;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.CommentInfo;
import com.example.sns_project.info.LetterInfo;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.model.ServerTimestamps;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.example.sns_project.util.Named.ALREADY_DONE;
import static com.example.sns_project.util.Named.CREATE;
import static com.example.sns_project.util.Named.DELETE;
import static com.example.sns_project.util.Named.HOUR;
import static com.example.sns_project.util.Named.MIN;
import static com.example.sns_project.util.Named.NOT_EXIST;
import static com.example.sns_project.util.Named.SEARCH_LIMIT;
import static com.example.sns_project.util.Named.SEC;
import static com.example.sns_project.util.Named.SUCCESS;
import static com.example.sns_project.util.Named.UPLOAD_LIMIT;

/**
 * use this with my_utility for requesting posts
 * in recycler_init() after my utility or in same time
 */

public class PostControler {

    private String post_location;
    private FirebaseFirestore Store = FirebaseFirestore.getInstance();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private My_Utility my_utility;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

    public PostControler(){}

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

    public interface Listener_Complete_Get_RoomsKey {
        void onComplete_Get_RoomsKey(boolean IsExist);
    }

    public interface Listener_Complete_Get_Letters {
        void onComplete_Get_Letters(ArrayList<LetterInfo> Letters);
        void onFail();
    }

    public interface Listener_NewLetter {
        void Listener_NewLetter(LetterInfo Letters);
    }

    public interface Listener_Room_Outdate {
        void GetOutdate_Room(Long OutDate);
        void Done();
    }

    public interface Listener_RoomKeys {
        void Listener_RoomKeys_User(String RoomKey);
    }

    public interface Listener_Get_RoomKeys {
        void GetRoomKeys(ArrayList<String> rooms);
    }

    public interface Listener_Get_Room {
        void onGetRoom(ChatRoomInfo room);
    }

    public void Search_Post(ArrayList<PostInfo> Loaded_Posts,String KeyWord, Listener_CompletePostInfos listener_completePostInfos){

        ArrayList<PostInfo> newPosts = new ArrayList<>();
        ArrayList<PostInfo> temp = DeepCopy_ArrayPostInfo(Loaded_Posts);

        Date OldestDate = Loaded_Posts.size() == 0 ? new Date() : Loaded_Posts.get(Loaded_Posts.size() - 1).getCreatedAt();

        Log.d("zozozozozo","시작: "+newPosts.size());

        Store.collection(post_location)
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
                                        newPosts.add(new PostInfo(Get_PostInfo_From_Doc(document)));
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

        Store.collection(post_location)
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
                                    newPosts.add(new PostInfo(Get_PostInfo_From_Doc(document)));
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
                                newPosts.add(new PostInfo( Get_PostInfo_From_Doc(document) ) );

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

            Store.collection(post_location).document(docid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
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

    public void Get_UniPost(String docid, Listener_Complete_Get_PostInfo listener_complete_get_postInfo){

        Store.collection(post_location).document(docid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        PostInfo postInfo = Get_PostInfo_From_Doc(documentSnapshot);
                        listener_complete_get_postInfo.onComplete_Get_PostInfo(postInfo);
                    }else{
                        my_utility.Toast("존재하지 않는 게시물입니다.");
                    }
            }
        });
    }

    public void Set_UniPost(PostInfo postInfo,Listener_Complete_Set_PostInfo listener_complete_set_postInfo){

        Store.collection(post_location).document(postInfo.getDocid()).update(postInfo.getPostInfo()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                listener_complete_set_postInfo.onComplete_Set_PostInfo();
            }
        });
    }

    public void Update_ReComments_With_Transaction(String docid,String Key, RecommentInfo NewRecomment,Listener_Complete_Set_PostInfo_Transaction completeListener){

        DocumentReference drf =  Store.collection(post_location).document(docid);

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

                for(int x=0; x<commentInfoArrayList.size(); x++){
                    if(commentInfoArrayList.get(x).getKey().equals(Key)) {
                        isexists = true;
                        position = x;
                        break;
                    }
                }

                if(isexists) {
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

        DocumentReference drf =  Store.collection(post_location).document(docid);

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

    public void Press_Good_Post(PostInfo postInfo, Listener_Complete_GoodPress complete_goodPress){ //좋아요 버튼 누르면 db의 해당 게시물의 좋아요수가 증가한다.

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //postinfo로 해당게시물에 좋아요를 누른 사람 id를 저장해주고,
        //좋아요 누른 사람이 중복으로 누르지않게 id를 찾아서 있으면 아닌거고 없으면 좋아요+1
        if(postInfo.getId().equals(user.getUid())){
            complete_goodPress.CannotSelf();
        }else {
            DocumentReference docref = Store.collection(postInfo.getLocation()).document(postInfo.getDocid());
            //처음 누른다면
            //이후에 DB처리
            Store.runTransaction(new Transaction.Function<My_Utility.Pair>() {
                @Override
                public My_Utility.Pair apply(Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(docref);

                    if(snapshot.exists()) {
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
                    }else
                        return new My_Utility.Pair(null, NOT_EXIST);
                }
            }).addOnSuccessListener(new OnSuccessListener<My_Utility.Pair>() {
                @Override
                public void onSuccess(My_Utility.Pair pair) {
                    if (pair.getIsSuccess() == SUCCESS)
                        complete_goodPress.onComplete_Good_Press((PostInfo) pair.getResult());
                    else if(pair.getIsSuccess() == ALREADY_DONE)
                        complete_goodPress.AlreadyDone();
                    else if(pair.getIsSuccess() == NOT_EXIST)
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

    public void Press_Good_Comment(PostInfo postInfo,int position,Listener_Complete_GoodPress complete_goodPress){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docref = Store.collection(postInfo.getLocation()).document(postInfo.getDocid());
        String Key = postInfo.getComments().get(position).getKey();

        if(postInfo.getComments().get(position).getId().equals(user.getUid())){ //셀프추천
            complete_goodPress.CannotSelf();
        }else {
            Store.runTransaction(new Transaction.Function<My_Utility.Pair>() {
                @Override
                public My_Utility.Pair apply(Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(docref);

                    if(snapshot.exists()) {
                        //읽기
                        PostInfo NewPostInfo = Get_PostInfo_From_Doc(snapshot);
                        ArrayList<CommentInfo> comments = NewPostInfo.getComments();
                        CommentInfo comment = comments.get(position);
                        int CommentIDX = 0;
                        CommentIDX = Find_Comment(comments,Key);

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
                        }else //댓글이 존재하지 않는다면
                            return new My_Utility.Pair(null, NOT_EXIST);
                    }else//포스트가 존재하지 않는다면
                        return new My_Utility.Pair(null, NOT_EXIST);
                }
            }).addOnSuccessListener(new OnSuccessListener<My_Utility.Pair>() {
                @Override
                public void onSuccess(My_Utility.Pair pair) {
                    if (pair.getIsSuccess() == SUCCESS)
                        complete_goodPress.onComplete_Good_Press((PostInfo) pair.getResult());
                    else if(pair.getIsSuccess() == ALREADY_DONE)
                        complete_goodPress.AlreadyDone();
                    else if(pair.getIsSuccess() == NOT_EXIST)
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

    public void Press_Good_ReComment(PostInfo postInfo,CommentInfo Parent_Comment,RecommentInfo recomment,Listener_Complete_GoodPress complete_goodPress){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docref = Store.collection(postInfo.getLocation()).document(postInfo.getDocid());

        if(recomment.getId().equals(user.getUid())){ //셀프추천
            complete_goodPress.CannotSelf();
        }else {
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
                                Log.d("wndqhr","중복추천");
                                Log.d("wndqhr","중복추천");Log.d("wndqhr","중복추천");Log.d("wndqhr","중복추천");Log.d("wndqhr","중복추천");
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
                            Log.d("wndqhr","삭제");    Log.d("wndqhr","삭제");    Log.d("wndqhr","삭제");    Log.d("wndqhr","삭제");    Log.d("wndqhr","삭제");
                            return new My_Utility.Pair(null, NOT_EXIST);
                        }
                    } else //포스트가 존재하지 않는다면
                    {
                        Log.d("wndqhr","shwhswo"); Log.d("wndqhr","shwhswo"); Log.d("wndqhr","shwhswo"); Log.d("wndqhr","shwhswo"); Log.d("wndqhr","shwhswo"); Log.d("wndqhr","shwhswo"); Log.d("wndqhr","shwhswo"); Log.d("wndqhr","shwhswo");
                        return new My_Utility.Pair(null, NOT_EXIST);

                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<My_Utility.Pair>() {
                @Override
                public void onSuccess(My_Utility.Pair pair) {
                    if (pair.getIsSuccess() == SUCCESS)
                        complete_goodPress.onComplete_Good_Press((PostInfo) pair.getResult());
                    else if(pair.getIsSuccess() == ALREADY_DONE)
                        complete_goodPress.AlreadyDone();
                    else if(pair.getIsSuccess() == NOT_EXIST)
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

    public void Find_Rooms_From_USER(String UID,String Key,Listener_Complete_Get_RoomsKey listener_complete_get_roomsKey){

        Store.collection("USER").document(UID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    ArrayList<String> RoomsKey = (ArrayList<String>)documentSnapshot.get("RoomKeys");
                    for(String Room : RoomsKey){
                        if(Room.equals(Key)){
                            listener_complete_get_roomsKey.onComplete_Get_RoomsKey(true);
                            break;
                        }
                    }
                    listener_complete_get_roomsKey.onComplete_Get_RoomsKey(false);
                }else
                    listener_complete_get_roomsKey.onComplete_Get_RoomsKey(false);
            }
        });
    }

    public void Set_RoomKeys_Listener_From_User(String id, Listener_Get_RoomKeys get_roomKeys){
        Store.collection("USER").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                ArrayList<String> rooms = (ArrayList<String>)value.get("RoomKeys");
                Log.d("tmxhdj",rooms.size()+": 룸갯수user");
                get_roomKeys.GetRoomKeys(rooms);
            }
        });
    }

    public void Get_Rooms_From_User(String id,Listener_Get_RoomKeys get_roomKeys){
        Store.collection("USER").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> NewRooms = new ArrayList<>();
                NewRooms = (ArrayList<String>)documentSnapshot.get("RoomKeys");
                get_roomKeys.GetRoomKeys(NewRooms);
            }
        });
    }

    public void Get_RoomInfo_From_DB(String Key, Listener_Get_Room listener_get_room){
        database.child("Rooms").child(Key).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                ChatRoomInfo room = dataSnapshot.getValue(ChatRoomInfo.class);
                listener_get_room.onGetRoom(room);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("GetRoomInfoFRomDB","왜 난리야 대체 : " +e.getCause());
            }
        });
    }

    public void Set_RoomKey_User(String id,String Key,int order){

        DocumentReference drf = Store.collection("USER").document(id);

        if(order == CREATE) {
            Store.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(drf);
                    ArrayList<String> Keys = (ArrayList<String>)snapshot.get("RoomKeys");
                    Keys.add(Key);
                    transaction.update(drf, "RoomKeys", Keys);
                    return null;
                }
            });
        }

        if(order == DELETE){
            Store.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(drf);
                    ArrayList<String> Keys = (ArrayList<String>)snapshot.get("RoomKeys");
                    Keys.remove(Key);
                    transaction.update(drf, "RoomKeys", Key);
                    return null;
                }
            });
        }
    }

    public void Update_letter(String Key,LetterInfo letterInfo){

        Map<String,Object> map = new HashMap<>();

        database.child("Rooms").child(Key).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ChatRoomInfo room = dataSnapshot.getValue(ChatRoomInfo.class);
                    if(room.getUser1_id().equals(letterInfo.getSender_id())){
                        Log.d("dksldho","user2: "+room.getUser2_count()+"");
                        Log.d("dksldho","user2 +1 : "+ (room.getUser2_count()+1) +"");
                        map.put("user2_count",room.getUser2_count()+1);
                        database.child("Rooms").child(Key).updateChildren(map);
                    }else{
                        Log.d("dksldho","user1: "+room.getUser1_count()+"");
                        Log.d("dksldho","user1 +1 : "+ (room.getUser1_count()+1) +"");
                        map.put("user1_count",room.getUser1_count()+1);
                        database.child("Rooms").child(Key).updateChildren(map);
                    }
                }
            }
        });

        database.child("Letters").child(Key).push().setValue(letterInfo);
    }

    public void Create_NewRoom(String Key,String my_nick, String my_id, String user2,String user2_id,Listener_Room_Outdate listener_room_outdate){

        database.child("Rooms").child(Key).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ //이미 있는 방이라면, 방에서 나간적이 있는지 체크해서 시간을 넘겨라
                    ChatRoomInfo room = dataSnapshot.getValue(ChatRoomInfo.class);

                    if(room.getUser1_id().equals(my_id)) {
                        listener_room_outdate.GetOutdate_Room(room.getUser1_OutDate());
                        listener_room_outdate.Done();
                    }else{
                        listener_room_outdate.GetOutdate_Room(room.getUser2_OutDate());
                        listener_room_outdate.Done();
                    }

                }else{ //새로만들어야 한다면,
                    ChatRoomInfo Room = new ChatRoomInfo(my_nick,my_id,new Date().getTime(),0,user2,user2_id,new Date().getTime(),0,Key);
                    database.child("Rooms").child(Key).setValue(Room);
                    listener_room_outdate.Done();
                }
            }
        });
    }

    public void Set_Before_Exit(String Key, String my_id,String latestMessage,Long latestDate){

        database.child("Rooms").child(Key).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ //이미 있는 방이라면,

                    ChatRoomInfo room = dataSnapshot.getValue(ChatRoomInfo.class);

                    if(room.getUser1_id().equals(my_id)){
                        Map<String,Object> map = new HashMap<>();
                        map.put("user1_count",0);
                        map.put("latestMessage",latestMessage);
                        map.put("latestDate",latestDate);
                        database.child("Rooms").child(Key).updateChildren(map);
                    }else{
                        Map<String,Object> map = new HashMap<>();
                        map.put("user2_count",0);
                        map.put("latestMessage",latestMessage);
                        map.put("latestDate",latestDate);
                        database.child("Rooms").child(Key).updateChildren(map);
                    }

                }
            }
        });
    }

    public void Set_LatestMessage(String Key,String latestMessage,Long latestDate) {

        Map<String, Object> map = new HashMap<>();
        map.put("latestMessage", latestMessage);
        map.put("latestDate", latestDate);

        database.child("Rooms").child(Key).updateChildren(map);
    }

    public void Set_Count_UP(String Key) {

        Map<String, Object> map = new HashMap<>();
        map.put("user1_count",0);
        database.child("Rooms").child(Key).updateChildren(map);

    }

    public void Listener_Room(String Key, Listener_Get_Room get_room){

        com.google.firebase.database.Query query = database.child("Rooms").child(Key).orderByChild("latestMessage").endAt(new Date().getTime());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                get_room.onGetRoom(snapshot.getValue(ChatRoomInfo.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void Set_Count_Zero(String Key, String my_id){

        database.child("Rooms").child(Key).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ //이미 있는 방이라면,

                    ChatRoomInfo room = dataSnapshot.getValue(ChatRoomInfo.class);

                    if(room.getUser1_id().equals(my_id)){
                        Map<String,Object> map = new HashMap<>();
                        map.put("user1_count",0);
                        database.child("Rooms").child(Key).updateChildren(map);
                    }else{
                        Map<String,Object> map = new HashMap<>();
                        map.put("user2_count",0);
                        database.child("Rooms").child(Key).updateChildren(map);
                    }
                }
            }
        });
    }


    public void Set_Outdate_Room(String Key,String User_id){
        Map<String,Object> map = new HashMap<>();

       database.child("Rooms").child(Key).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
           @Override
           public void onSuccess(DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists()){
                   ChatRoomInfo room = dataSnapshot.getValue(ChatRoomInfo.class);

                   if(room.getUser1_id().equals(User_id)){
                       map.put("user1_OutDate",new Date().getTime());
                       database.child("Rooms").child(Key).updateChildren(map);
                   }else{
                       map.put("user2_OutDate",new Date().getTime());
                       database.child("Rooms").child(Key).updateChildren(map);
                   }

               }
           }
       });
    }

    public void Delete_Room(String Key){
        database.child("Rooms").child(Key).setValue(null);
    }

    public void Set_RealtimeListener_onLetters(String Key, Listener_NewLetter changed){

        com.google.firebase.database.Query query = database.child("Letters").child(Key)
                .orderByChild("createdAt")
                .startAt(new Date().getTime());

        query.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
               changed.Listener_NewLetter(snapshot.getValue(LetterInfo.class));
           }
           @Override
           public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
           @Override
           public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
           @Override
           public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
           @Override
           public void onCancelled(@NonNull DatabaseError error) {}
       });
    }

    public void Bring_Letters(String Key,ArrayList<LetterInfo> Letters,Listener_Complete_Get_Letters complete_get_room){

        ArrayList<LetterInfo> NewLetters = Letters == null ? new ArrayList<>() : new ArrayList<>(Letters);
//        ArrayList<LetterInfo> temp =  new ArrayList<>();

//        Date latestDate = NewLetters.size() == 0 ? new Date() :new Date(NewLetters.get(0).getCreatedAt());
//
//        Log.d("tlrksd","size : "+NewLetters.size()+" date: "+ latestDate);
//        Log.d("tlrksd","size : "+NewLetters.size()+" date: "+ latestDate + "time: "+ ServerValue.TIMESTAMP);
//
//
//        com.google.firebase.database.Query query = database.child("Letters").child(Key)
//                .orderByChild("createdAt")
//                .startAt(Long.MIN_VALUE)
//                .endAt(latestDate.getTime())
//                .limitToLast(UPLOAD_LIMIT);
//
//        ValueEventListener valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
//                    temp.add(postSnapshot.getValue(LetterInfo.class)); //임시 저장소에 하나씩 받아서 저장
//                    if(temp.size() == snapshot.getChildrenCount()) { // 나올만큼 나왔으면 한번에 데이터를 리스너에 넘긴다.
//                        if(NewLetters.size() != 0)
//                            temp.remove(temp.size()-1); //처음 불러오는 메시지 외에는 마지막항이 이전의 첫번째랑 중복되게 쿼리가 나오기때문에 제거하고 넘긴다.
//                        NewLetters.addAll(0,temp); //통째로 앞쪽에 위치시키기위해서 0번째로 넣고 나머지는 뒤로 밀려나가게 했음.
//                        complete_get_room.onComplete_Get_Letters(NewLetters);
//                    }
//                    Log.d("tlrksd","temp_size : "+temp.size()+" snapshot_size: "+ snapshot.getChildrenCount());
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {}
//        };
//        query.addValueEventListener(valueEventListener);

        database.child("Letters").child(Key).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                for(Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator(); iter.hasNext(); ){
                    NewLetters.add(iter.next().getValue(LetterInfo.class));
                    Log.d("anjfrkwudhsk",NewLetters.size()+"");

                    if(!iter.hasNext())
                        complete_get_room.onComplete_Get_Letters(NewLetters);
                }
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
//        } else if ((diffTime /= MIN) < HOUR) {
//            msg = new SimpleDateFormat("HH:mm").format(postdate);
//        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
//            msg = (diffTime) + "일 전";
//        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
//            msg = (diffTime) + "달 전";
        } else {
            msg = new SimpleDateFormat("HH:mm").format(new Date(createdAt));
            int hour = (Character.getNumericValue(msg.charAt(0))*10 + Character.getNumericValue(msg.charAt(1)));
            int min = (Character.getNumericValue(msg.charAt(3))*10 + Character.getNumericValue(msg.charAt(4)));
            if(hour > 12) {
                msg = "오후 "+ (hour-12) + ":" + String.format("%02d", min);
            }else
                msg = "오전 " + msg;
        }

//        Log.d("acdksld",msg);
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
            msg = new SimpleDateFormat("MM월dd일").format(postdate);
        }
        return msg;
    }

}
