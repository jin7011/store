package com.example.sns_project.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sns_project.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PermissionActivity extends AppCompatActivity {

    public final String TAG = MainActivity.class.getSimpleName();
    public ArrayList<String> permissions = new ArrayList<>(); // 요청해야 할 권한을 넣어 두기 위함
    private static final int REQ_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //todo 가끔 여기서 java.lang.IllegalStateException: Only fullscreen activities can request orientation
        setContentView(R.layout.activity_permission);

        if (!isGrantedPermission()) {
            createPermissionDialog();
        } else {
            Activity(MainActivity.class);
        }
    }

    public void Activity(Class c){
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(i);
        this.finish();
    }

    public boolean isGrantedPermission() {

        permissions.clear();

        Log.d(TAG, " checkSelfPermission CAMERA : " + ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA));
        Log.d(TAG, " checkSelfPermission AUDIO : " + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE));

        //  카메라
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }

        // READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // 권한이 없으면 permissions ArrayList에 추가 해 놓는다
        // 즉 리턴값은 허가 받아야 할 권한을 ArrayList 저장해 놓고 권한을 요청여부를 리턴 한다
        return permissions.size() == 0;
    }

    public void createPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("권한 요청 안내");
        builder.setMessage("다음과 같은 권한이 필요 합니다 \n[저장소],[카메라]");
        builder.setCancelable(false);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermission();
            }
        });
        builder.show();
    }

    private void requestPermission() {
        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]), REQ_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int grantedCnt = 0;
        switch (requestCode) {
            case REQ_PERMISSION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // 허용한 권한 갯수를 카운트 한다
                            grantedCnt++;
                        }
                    }
                }
                break;
        }

        // 요청권한 갯수와 사용자가 승인한 허용 갯수가 맞으면 다음 로직을 진행 한다.
        // 숫자가 맞지 안을 경우 다시 권한 요청을 의뢰 한다
        if (grantedCnt == permissions.length) {
            // 권한 승인 이후 하고자 하는 액션을 취한다.
            Activity(MainActivity.class);
        } else {
            // 필요한 권한을 전부 허용 하지 않았을 경우 다른 방법으로 재 요청 처리를 해 보았다
            createSettingsDialog();
        }
    }

    private void createSettingsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("앱을 원할이 이용하기 위해서 저장소, 카메라 권한이 필요 합니다.");
        builder.setCancelable(false);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermission();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        builder.show();
    }
}