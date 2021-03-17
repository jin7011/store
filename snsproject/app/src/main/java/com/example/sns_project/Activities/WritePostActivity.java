package com.example.sns_project.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sns_project.Info.WritePost;
import com.example.sns_project.databinding.ActivityWritePostBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WritePostActivity extends AppCompatActivity {
    ActivityWritePostBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private WritePost writePost;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWritePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        binding.cancelBtn.setOnClickListener(new View.OnClickListener() { //뒤로가기
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.postBtn.setOnClickListener(new View.OnClickListener() { //이제 글을 데이터에 등록
            @Override
            public void onClick(View view) {
                String title = binding.titleEdit.getText().toString();
                String content = binding.contentEdit.getText().toString();

                if(title.length() >= 2 && content.length() >= 2){
                    UploadPost(user.getUid(),title,content);
                }else{
                 Tost("제목 및 내용을 2글자 이상 입력해주세요.");
                }
            }
        });

    }

    private void UploadPost(String title, String content, String uid) {

        Map<String, Object> data = new HashMap<>();
        data.put(writePost.ID, uid);
        data.put(writePost.title, title);
        data.put(writePost.content, content);

        db.collection(writePost.Post).add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

}