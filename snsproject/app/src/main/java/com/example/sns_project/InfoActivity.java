package com.example.sns_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class InfoActivity extends AppCompatActivity {

    EditText BusinessNum;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    UserProfile userprofile = new UserProfile();
    EditText AreaE,CareerE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        AreaE = findViewById(R.id.Area);
        CareerE = findViewById(R.id.Career);
        BusinessNum = findViewById(R.id.mail);
        Button Enroll = findViewById(R.id.Enroll_btn);
        Enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String num = BusinessNum.getText().toString();

                if(check(num))
                BusinessNumberUpdate(num);

            }
        });

    }

    public void BusinessNumberUpdate(String num){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //사업자번호 파싱하는 함수만들어야함 func();

        Map<String, Object> data = new HashMap<>();
        data.put(userprofile.ID, user.getUid());
        data.put(userprofile.area, AreaE.getText().toString());
        data.put(userprofile.career, CareerE.getText().toString());
        db.collection(userprofile.USER).document(user.getUid()).set(data, SetOptions.merge());
        Tost("등록되었습니다.");

    }

    public boolean check(String num){
        if(num.length() < 1){
            Tost("번호를 다시 확인해주세요.");
            return false;
        }
        else
            return true;
    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
}