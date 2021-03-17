package com.example.sns_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Sign_up extends AppCompatActivity {
    private FirebaseAuth mAuth;
    EditText mail, pass, passcheck;
    Spinner spinner;
    CheckBox checkBox;
    boolean checked = false;
    String location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        spinner = findViewById(R.id.spinner_location);
        mAuth = FirebaseAuth.getInstance();
        mail = findViewById(R.id.mail);
        pass = findViewById(R.id.pass);
        passcheck = findViewById(R.id.pass_check);
        checkBox = findViewById(R.id.checkBox_agree);

        String[] arr = getResources().getStringArray(R.array.my_array);
        ArrayAdapter adapter = new ArrayAdapter(getBaseContext(),R.layout.spinner_item,arr);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                location = arr[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button button = findViewById(R.id.signup_btn);
        Button gologin = findViewById(R.id.gologin_btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String m = mail.getText().toString();
                String p = pass.getText().toString();
                String pc = passcheck.getText().toString();

                if(!checkBox.isChecked()){
                    Tost("동의하기 버튼에 체크해주세요.");
                    return;
                }

                if(inputcheck(m,p,pc) && checkBox.isChecked())
                    join(m,p);

            }
        });

        gologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void join(String m, String p) {

        mAuth.createUserWithEmailAndPassword(m, p)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Tost("가입되었습니다!");
                            FirebaseUser user = mAuth.getCurrentUser();
                            MainActivity();
                        } else {
                           Tost("이메일형식/비밀번호를 확인해주세요.");
                        }
                        // ...
                    }
                });
    }

    public boolean inputcheck(String m,String p,String pc){

        if(m.length() < 1) {
            Tost("이메일을 입력해주세요.");
            return false;
        }
        if(p.length() < 6) {
            Tost("비밀번호를 6글자 이상 입력해주세요.");
            return false;
        }
        if (!p.equals(pc)) {
            Tost("비밀번호가 다릅니다.");
            return false;
        }else
            return true;

    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    public void LoginActivity(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    public void MainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}