package com.example.sns_project.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.Info.PostInfo;
import com.example.sns_project.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private ArrayList<PostInfo> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if(user == null){
            Activity(SignActivity.class);
        }
        else{
            init();
        }

        binding.mainToolbar.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity(WritePostActivity.class);
            }
        });
    }

    private void init() {

        DocumentReference locationDoc = db.collection("USER").document(user.getUid());

        locationDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) { //USER에서 location을 찾는 것은 비동기적이기떄문에 함수화 못했꼬, 그래서 우선적으로 지역을 찾자

                        String location = document.getString("location"); //USER안에서 location을 찾아오는 쿼리(?)
                        Log.d("지격탐색(main)",location);

                        binding.mainToolbar.locationToolbarText.setText(location);

                        db.collection(location)
                               .orderBy("createdAt", Query.Direction.DESCENDING)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            int fcnt = 0;
                                            int cnt = 0;
                                            postList = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Log.d("가져옴", document.getId() + " => " + document.getData());

                                                if(document.getData().get("formats") != null) {
                                                    postList.add(new PostInfo(
                                                                    user.getUid(),
                                                                    document.get("publisher").toString(),
                                                                    document.get("title").toString(),
                                                                    document.get("contents").toString(),
                                                                    (ArrayList<String>) document.getData().get("formats"),
                                                                    new Date(document.getDate("createdAt").getTime()),
                                                                    document.getId(),
                                                                    0, 0
                                                            )
                                                    );
                                                    fcnt++;
                                                }
                                                else{
                                                    postList.add(new PostInfo(
                                                                    user.getUid(),
                                                                    document.get("publisher").toString(),
                                                                    document.get("title").toString(),
                                                                    document.get("contents").toString(),
                                                                    new Date(document.getDate("createdAt").getTime()),
                                                                    document.getId(),
                                                                    0,0
                                                            )
                                                    );
                                                    cnt++;
                                                }
                                            }
                                            Log.d("가져옴", "포멧게시글갯수: "+fcnt+"  걍게시글: "+cnt);
                                            Add_and_SetRecyclerView(MainActivity.this,postList);
                                        } else {
                                            Log.d("실패함", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        init();
//    }

    public void Add_and_SetRecyclerView(Activity activity, ArrayList<PostInfo> postList){

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.postRecycler.setLayoutManager(layoutManager);

        PostAdapter postAdapter = new PostAdapter(activity, postList);
        binding.postRecycler.setAdapter(postAdapter);

    }
    public void Activity(Class c){
        Intent intent = new Intent(this,c);
        startActivity(intent);
    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

}
