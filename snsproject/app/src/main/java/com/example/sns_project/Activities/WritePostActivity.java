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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sns_project.Adapter.AddImageAdapter;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_WritePost;
import com.example.sns_project.databinding.ActivityWritePostBinding;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.Named;
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

import static com.example.sns_project.util.Named.WriteResult;

public class WritePostActivity extends AppCompatActivity {
    ActivityWritePostBinding binding;

    private long backKeyPressedTime = 0;
    private final int REQ_PICK_IMAGE_VIDEO = 1;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private PostInfo postInfo;
    private ArrayList<Uri> UriFormats = new ArrayList<>();
    private RelativeLayout loaderView;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String location;
    private LiveData_WritePost Postmodel;
    private Named named = new Named();

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

        //라이브데이터
        Postmodel = new ViewModelProvider(WritePostActivity.this).get(LiveData_WritePost.class);
        Postmodel.get().observe(this, new Observer<ArrayList<Uri>>() {
            @Override
            public void onChanged(ArrayList<Uri> uris) {
                Toast(uris.size()+"");
                Add_and_SetRecyclerView(WritePostActivity.this);
            }
        });

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
                 Toast("제목 및 내용을 2글자 이상 입력해주세요.");
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

        if (resultCode == RESULT_OK && requestCode == REQ_PICK_IMAGE_VIDEO) { //갤러리 선택에서 온 request
            Uri uri = data.getData();
            if(Postmodel.get().getValue() != null) {
                UriFormats = Postmodel.get().getValue();
                UriFormats.add(uri);
                Postmodel.get().setValue(UriFormats);
            }else{
                UriFormats.add(uri);
                Postmodel.get().setValue(UriFormats);
            }
        }

    }

    public void Add_and_SetRecyclerView(Activity activity){

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.ImageRecycler.setLayoutManager(layoutManager);

        AddImageAdapter addImageAdapter = new AddImageAdapter(activity);
        binding.ImageRecycler.setAdapter(addImageAdapter);

    }

    public void UploadStorage(String uid, String nickname, String title, String content) {
        loaderView.setVisibility(View.VISIBLE);

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
                        final ArrayList<String> storagePath = new ArrayList<>();
                        UriFormats = Postmodel.get().getValue(); //최종적으로 라이브데이터의 리스트를 가져옴
                        postInfo = new PostInfo(uid, nickname, title, content,date,documentReference.getId(),location);

                        if(UriFormats != null) {
                            Log.d("imageList"," 갯수: "+UriFormats.size());
                            uploadPosts(UriFormats,documentReference,formatList,storagePath,postInfo);

                        }else{ //파일없이 글만 올리는 경우
                            UploadPost(documentReference, postInfo);
                        }
                    }

                }
            }
        });
    }

    private void uploadPosts(final ArrayList<Uri> mediaUris, DocumentReference documentReference, final ArrayList<String> formatList, ArrayList<String> storagePath, PostInfo postInfo) {

        if(formatList.size() == mediaUris.size())
            return;

        Log.d("imageList"," 갯수: "+mediaUris.size()+"formatList.size(): "+formatList.size());

        InputStream stream = null;
        if(mediaUris.size() != 0 && mediaUris != null)
        try {
            stream = new FileInputStream(new File(getPathFromUri(mediaUris.get(formatList.size()))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        else return;

        if(stream != null) {

            String[] pathArray = getPathFromUri(mediaUris.get(formatList.size())).split("\\.");
            String storagepath = location+"/"+ documentReference.getId() + "/" + formatList.size() + "." + pathArray[pathArray.length - 1];
            final StorageReference fileRef = storageRef.child(storagepath);
            StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + formatList.size()).build();
            final UploadTask uploadTask = fileRef.putStream(stream, metadata);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("TAG", "URL = " + uri); //url of each file

                            if(formatList.size() < mediaUris.size()) {
                                formatList.add(uri.toString()); //다운로드를 위한 uri
                                storagePath.add(storagepath); //이후에 storage 수정과 삭제를 위한 storage경로
                                uploadPosts(mediaUris, documentReference, formatList, storagePath, postInfo); //Recursion
                                Log.d("포멧올리는 과정", "size: " + formatList.size()+"medi size : "+mediaUris);
                            }
                            if (formatList.size() == mediaUris.size()) {
                                Log.d("한번만 튀어나오면댐", "제발: " + formatList.size());
                                postInfo.setFormats(formatList);
                                postInfo.setStoragePath(storagePath);
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
                        Toast("성공적으로 게시되었습니다.");
                        loaderView.setVisibility(View.GONE);
                        toMain(WriteResult);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast("업로드에 실패하였습니다.");
                        loaderView.setVisibility(View.GONE);
                    }
                });

    }

    public void toMain(int request){
        Intent intent = new Intent();
        setResult(request,intent);
        finish();
    }

    public String getPathFromUri(Uri uri){

        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();

        return path;
    }

    @Override
    public void onBackPressed() {
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
        if (System.currentTimeMillis() > backKeyPressedTime + 1500) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast("\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.");
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 1500) {
            //아래 3줄은 프로세스 종료
            finish();
        }

    }

    public void Toast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
}

