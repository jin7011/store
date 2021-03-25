package com.example.sns_project.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sns_project.Adapter.AddImageAdapter;
import com.example.sns_project.Info.ImageList;
import com.example.sns_project.Info.PostInfo;
import com.example.sns_project.R;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

public class WritePostActivity extends AppCompatActivity {
    ActivityWritePostBinding binding;

    private final int REQ_PICK_IMAGE_VIDEO = 1;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private int fileNum = 0;
    private PostInfo postInfo;
    private com.example.sns_project.Info.ImageList imageList = ImageList.getimageListInstance();
    private RelativeLayout loaderView;
    private  FirebaseStorage storage;
    private StorageReference storageRef;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWritePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loaderView = findViewById(R.id.loaderLyaout);

        //인증init
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        binding.writeToolbar.backBtn.setOnClickListener(new View.OnClickListener() { //뒤로가기
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.writeToolbar.postBtn.setOnClickListener(new View.OnClickListener() { //이제 글을 데이터에 등록
            @Override
            public void onClick(View view) {
                String title = binding.titleEdit.getText().toString();
                String content = binding.contentEdit.getText().toString();

                if(title.length() >= 2 && content.length() >= 2){
                    UploadStorage(user.getUid(),user.getDisplayName(),title,content);
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

            if (uri.toString().contains("image")) { //어쨌든 뭐라도 하나 추가했다면, -> 아이템 등록
                imageList.add(uri);
                Add_and_SetRecyclerView(WritePostActivity.this);

            } else if (uri.toString().contains("video")) {
                imageList.add(uri);
                Add_and_SetRecyclerView(WritePostActivity.this);
            }
        }
    }

    public void UploadStorage(String uid, String nickname, String title, String content) {
        loaderView.setVisibility(View.VISIBLE);
        fileNum = 0;

        DocumentReference locationDoc = db.collection("USER").document(uid);

        locationDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) { //USER에서 location을 찾는 것은 비동기적이기떄문에 함수화 못했꼬, 그래서 우선적으로 지역을 찾고 찾았다면, 포스트를 올리기로.

                        location = document.getString("location"); //USER안에서 location을 찾아오는 쿼리(?)
                        Log.d("지격탐색",location);

                        final DocumentReference documentReference = postInfo == null ? db.collection(location).document() : db.collection(location).document(postInfo.getId());
                        final Date date = postInfo == null ? new Date() : postInfo.getCreatedAt();
                        final ArrayList<String> formatList = new ArrayList<>();
                        postInfo = new PostInfo(uid, nickname, title, content, date);

                        Log.d("imageList"," 갯수: "+imageList.getImageList().size()+"filenum: "+fileNum);
                        if(imageList.getImageList().size() !=0) {
                            uploadPosts(imageList.getImageList(),documentReference,formatList,postInfo);

                        }else{ //파일없이 글만 올리는 경우
                            UploadPost(documentReference, postInfo);
                        }
                    }

                }
            }
        });
    }

    private void uploadPosts(final ArrayList<Uri> mediaUris,DocumentReference documentReference,final ArrayList<String> formatList,PostInfo postInfo) {

        Log.d("imageList"," 갯수: "+mediaUris.size()+"filenum: "+fileNum);

        InputStream stream = null;
        if(mediaUris.size() != 0 && mediaUris != null)
        try {
            stream = new FileInputStream(new File(getPathFromUri(mediaUris.get(fileNum))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(stream != null) {
            String[] pathArray = getPathFromUri(mediaUris.get(fileNum)).split("\\.");
            final StorageReference fileRef = storageRef.child(location+"/"+ documentReference.getId() + "/" + fileNum + "." + pathArray[pathArray.length - 1]);
            StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + fileNum).build();
            final UploadTask uploadTask = fileRef.putStream(stream, metadata);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("TAG", "URL = " + uri); //url of each file

                            if(fileNum < mediaUris.size()) {
                                formatList.add(uri.toString());
                                uploadPosts(mediaUris, documentReference, formatList, postInfo); //Recursion
                                Log.d("포멧올리는 과정", "size: " + formatList.size()+"filenum: "+fileNum+"medi size : "+mediaUris);
                                fileNum++;
                            }
                            if (formatList.size() == mediaUris.size()) {
                                Log.d("한번만 튀어나오면댐", "제발: " + formatList.size());
                                postInfo.setFormats(formatList);
                                UploadPost(documentReference, postInfo);
                                return;
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                            Log.e("TAG", "Failed " + e.getCause());
                        }
                    });
                }
            });
        }
    }
        private void UploadPost(DocumentReference documentReference,final PostInfo postInfo) {

        documentReference.set(postInfo.getPostInfo())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Tost("성공적으로 게시되었습니다.");
                        loaderView.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Tost("업로드에 실패하였습니다.");
                        loaderView.setVisibility(View.GONE);
                        finish();
                    }
                });

    }


//          for (int x = 0; x < imageList.getImageList().size();) {
//
//        fileNum=x; //0부터 시작 (파일 0개, 1개 ...)  // try문 밖에서 사용함으로써 안정적으로 숫자를 카운트가능 (이전에 try안에 넣어서 오류났었음)
//        String[] pathArray = getPathFromUri(imageList.getImageList().get(x)).split("\\.");
//        formatList.add(pathArray[pathArray.length - 1]);
//final StorageReference mountainImagesRef = storageRef.child(location+"/"+ documentReference.getId() + "/" + x + "." + pathArray[pathArray.length - 1]);
//
//        try {
//        Log.d("fileNUm 체크","filenum: " + fileNum);
//        InputStream stream = new FileInputStream(new File(getPathFromUri(imageList.getImageList().get(x)))); //경로
//        StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + fileNum).build();
//        UploadTask uploadTask = mountainImagesRef.putStream(stream,metadata);
//
//        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { //storage에 업로드 리스너
//@Override
//public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//        Log.d("사진업로드","일단 성공이고 index: "+fileNum);
//final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
//        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//@Override
//public void onSuccess(Uri uri) {
//        formatList.set(index,uri.toString());
//        if (fileNum == (imageList.getImageList().size()-1)) {
//        Log.d("포멧올리는 과정","size: "+imageList.getImageList().size());
//        PostInfo postInfo = new PostInfo(uid, nickname, title, content, formatList, date);
//        UploadPost(documentReference, postInfo);
//        }
//        }
//        });
//        }
//        }).addOnFailureListener(new OnFailureListener() {
//@Override
//public void onFailure(@NonNull Exception exception) {
//        Tost("어라..? 망;");
//        loaderView.setVisibility(View.GONE);
//        }
//        });
//        } catch (FileNotFoundException e) {
//        loaderView.setVisibility(View.GONE);
//        e.printStackTrace();
//        }
//        }

    public void Add_and_SetRecyclerView(Activity activity){

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.ImageRecycler.setLayoutManager(layoutManager);

        AddImageAdapter addImageAdapter = new AddImageAdapter(activity);
        binding.ImageRecycler.setAdapter(addImageAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageList.clear();
    } // 글쓰기 종료시 저장한 사진데이터 모두 클리어.

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    public String getPathFromUri(Uri uri){

        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();

        return path;
    }

}

