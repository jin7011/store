package com.example.sns_project.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sns_project.Info.WritePost;
import com.example.sns_project.View.Post_ImageView;
import com.example.sns_project.databinding.ActivityWritePostBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WritePostActivity extends AppCompatActivity {
    ActivityWritePostBinding binding;
    private FirebaseUser user;
    private WritePost writePost = new WritePost();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final int REQ_PICK_IMAGE_VIDEO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWritePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();

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
                    UploadPost(user.getUid(),user.getDisplayName(),title,content);
                }else{
                 Tost("제목 및 내용을 2글자 이상 입력해주세요.");
                }
            }
        });

        binding.addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/* video/*");
                startActivityForResult(intent, REQ_PICK_IMAGE_VIDEO);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQ_PICK_IMAGE_VIDEO) {
            Uri uri = data.getData();

            if (uri.toString().contains("image")) {
                Uri imageURI = data.getData();

                Post_ImageView post_imageView = new Post_ImageView(this,imageURI);
                post_imageView.makeImage();

            } else if (uri.toString().contains("video")) {
                Uri imageURI = data.getData();

                Post_ImageView post_imageView = new Post_ImageView(this,imageURI);
                post_imageView.makeImage();
            }
        }

    }


    private void UploadPost(String uid,String nickname,String title, String content) {

        Map<String, Object> data = new HashMap<>();
        data.put(writePost.ID, uid);
        data.put(writePost.nickname, nickname);
        data.put(writePost.title, title);
        data.put(writePost.content, content);

        DocumentReference docRef = db.collection("USER").document(uid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        String location = document.getString("location"); //데이터베이스의 uid를 찾아서 location을 추출한 후에
                        db.collection(location).add(data) //location을 collection으로 저장하여 게시판 세분화
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                       //메인으로 다시 이동할 예정.
                                         Tost("게시글이 작성되었습니다.");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Tost("문제가 발생하였습니다.");
                            }

                        });
                    }
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // recyclerInit();
                    Tost("시작");
                } else {
                    Tost("권한을 허락해주세요.");
                }
            }
        }
    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }


}
