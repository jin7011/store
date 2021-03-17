package com.example.sns_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sns_project.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String location;
    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        String[] arr = getResources().getStringArray(R.array.my_array);
        ArrayAdapter adapter = new ArrayAdapter(getBaseContext(),R.layout.spinner_item,arr);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        set_SpinnerAdapter(adapter,arr);

        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String m = binding.mail.getText().toString();
                String p = binding.pass.getText().toString();
                String pc = binding.passCheck.getText().toString();
                String nickname = binding.nickname.getText().toString();
                String phonNum = binding.phoneNum.getText().toString();
                String storename = binding.storeName.getText().toString();

                if(!binding.checkBoxAgree.isChecked()){
                    Tost("동의하기 버튼에 체크해주세요.");
                    return;
                }

                if(inputcheck(m,p,pc,nickname,phonNum,storename,location) && binding.checkBoxAgree.isChecked())
                    join(m,p);
            }
        });

        binding.gologinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
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

    public boolean inputcheck(String m, String p, String pc, String nickname, String phone, String store, String location){
        boolean[] flag = {false,false,false,false,false,false};
        if(nickname.length() >= 1 && m.length() >= 1)
            flag[0] = true;
        if(p.length() >= 6)
            flag[1] = true;
        if (p.equals(pc))
            flag[2] = true;
        if(phone.length() >= 8)
            flag[3] = true;
        if(store.length() >= 1)
            flag[4] = true;
        if(location.length() >= 1)
            flag[5] = true;
        for(int x=0; x<flag.length; x++){
            switch (x){
                case 0:
                    if(!flag[x]) {
                        Tost("닉네임/이메일을 입력해주세요.");
                        return false;
                    }
                case 1:
                    if(!flag[x]) {
                        Tost("비밀번호를 6글자 이상 입력해주세요.");
                        return false;
                    }
                case 2:
                    if(!flag[x]) {
                        Tost("비밀번호가 다릅니다.");
                        return false;
                    }
                case 3:
                    if(!flag[x]) {
                        Tost("번호 입력해주세요.");
                        return false;
                    }
                case 4:
                    if(!flag[x]) {
                        Tost("사업장명을 입력해주세요.");
                        return false;
                    }
                case 5:
                    if(!flag[x]) {
                        Tost("장소를 선택해주세요.");
                        return false;
                    }
            }
        }
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

    public void set_SpinnerAdapter( ArrayAdapter adapter,String[] arr ){
        binding.spinnerLocation.setAdapter(adapter);
        binding.spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                location = arr[i];
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                location ="";
            }
        });
    }
}