package com.example.sns_project.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.sns_project.Adapter.Change_LocationAdapter;
import com.example.sns_project.R;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.util.My_Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.sns_project.util.Named.CHANGED_LOCATION;
import static com.example.sns_project.util.Named.GRID;

public class PopupActivity extends Activity {

    private MyAccount myAccount;
    private FirebaseFirestore db;
    private  String Selected_Location;
    private TextView textView;
    private  Button cancel;
    private  My_Utility my_utility;
    private  Button confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup);

        db = FirebaseFirestore.getInstance();

        textView = findViewById(R.id.mylocationT);
        cancel = findViewById(R.id.CancelChangeLocation_btn);
        confirm = findViewById(R.id.ChangeLocation_btn);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        Intent intent = getIntent();
        myAccount = (MyAccount)intent.getParcelableExtra("myAccount");

        textView.setText(myAccount.getLocation());
        String[] arr =  getResources().getStringArray(R.array.my_array);

        RecyclerView recyclerView = findViewById(R.id.ChangeLocation_RecyclerView);
        Change_LocationAdapter adapter = new Change_LocationAdapter(this,arr,myAccount.getLocation());
        my_utility = new My_Utility(this,recyclerView,adapter);
        my_utility.RecyclerInit(GRID);
    }

    public void setTextView(String str){
        textView.setText(str);
    }

    public void confirm(){

        if(Selected_Location == null || Selected_Location.length() == 0 || Selected_Location.equals(myAccount.getLocation())) {
            my_utility.Toast("다른지역을 선택해주세요.");
        }else {
            db.collection("USER").document(myAccount.getId()).update("location", Selected_Location).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent();
                    setResult(CHANGED_LOCATION, intent);
                    finish();
                }
            });
        }
    }

    public void setSelected_Location(String str){
        Selected_Location = str;
    }

}