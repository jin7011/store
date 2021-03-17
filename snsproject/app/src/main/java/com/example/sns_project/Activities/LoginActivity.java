package com.example.sns_project.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sns_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText mail, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mail = findViewById(R.id.mail);
        pass = findViewById(R.id.pass);

        Button login = findViewById(R.id.login_btn);
        Button gosignup  = findViewById(R.id.gosignup_btn);
        Button findpass = findViewById(R.id.Findpass);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String m = mail.getText().toString();
                String p = pass.getText().toString();

                if(check(m,p))
                    Login(m,p);

            }
        });

        gosignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasswordActivity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    public boolean check(String m,String p){

        if(m.length() <2 || p.length() <6){
            Tost("아이디 또는 비밀번호를 확인해주세요.");
            return false;
        }else
            return true;

    }

    public void Login(String m,String p){
        mAuth.signInWithEmailAndPassword(m, p)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Tost("로그인 되었습니다.");
                            MainActivity();
                        } else {
                            Tost("로그인 실패하였습니다.");
                        }

                        // ...
                    }
                });
    }

    public void MainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void PasswordActivity(){
        Intent intent = new Intent(this, Password_resetActivity.class);
        startActivity(intent);
    }
}

