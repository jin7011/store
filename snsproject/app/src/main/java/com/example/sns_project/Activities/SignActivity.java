package com.example.sns_project.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sns_project.R;
import com.example.sns_project.databinding.ActivitySignUpBinding;
import com.example.sns_project.info.MyAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class SignActivity extends AppCompatActivity {
    private String BUSINESSNUMBER = "";
    final String[] ANSWER = new String[1];
    private boolean businessNumCheck;
    private long backKeyPressedTime = 0;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String location;
    private ActivitySignUpBinding binding;
    private RelativeLayout loader;
    private MyAccount myAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loader = findViewById(R.id.loaderLyaout);

        //init
        mAuth = FirebaseAuth.getInstance();

        String[] arr = getResources().getStringArray(R.array.my_array);
        ArrayAdapter adapter = new ArrayAdapter(getBaseContext(),R.layout.item_spinner,arr);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        set_SpinnerAdapter(adapter,arr);

        binding.BNCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String businessNum = binding.businessNum.getText().toString();

                if(businessNum.length() == 10) {
                    request(businessNum);
                    if (ANSWER[0] != null) {
                        if (BN_CheckString(ANSWER[0])) {
                            businessNumCheck = true;
                            binding.businessNum.setFocusableInTouchMode(false);
                            BUSINESSNUMBER = businessNum;
                        }
                    }
                }
                else{
                    Tost("사업자등록번호를 입력해주세요.");
                }
            }
        });

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

                if(!businessNumCheck){
                    Tost("사업자번호를 확인버튼을 눌러 검증해주세요.");
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

        loader.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(m, p)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUserProfile(user,binding.nickname.getText().toString());
                        } else {
                           Tost("이메일형식이 아니거나 이미 가입된 이메일입니다.");
                            loader.setVisibility(View.GONE);
                        }
                        // ...
                    }
                });
    }

    public void updateUserProfile(FirebaseUser user,String nickname){

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nickname)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            myAccount = new MyAccount(user.getUid(),user.getDisplayName(),"no",location,binding.storeName.toString(),
                                    binding.phoneNum.getText().toString(),BUSINESSNUMBER);
                            db.collection("USER").document(user.getUid()).set(myAccount.getMap(), SetOptions.merge());
                            Log.d("updateUserProfile", "User profile updated.");
                            loader.setVisibility(View.GONE);
                            MainActivity(myAccount);
                        }
                    }
                });
    }

    public boolean inputcheck(String m, String p, String pc, String nickname, String phone, String store,String location){
        boolean[] flag = {false,false,false,false,false,false,false};
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

    public void request(String str){

        //JSON형식으로 데이터 통신
        String url = "https://jinlab.herokuapp.com/";
        JSONObject testjson = new JSONObject();

        try {
            //입력해둔 edittext의 id와 pw값을 받아와 put해줍니다 : 데이터를 json형식으로 바꿔 넣어주었습니다.
            testjson.put("id",str);
            String jsonString = testjson.toString(); //완성된 json 포맷

            //이제 전송
            final RequestQueue requestQueue = Volley.newRequestQueue(SignActivity.this);
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,testjson, new Response.Listener<JSONObject>() {

                //데이터 전달을 끝내고 이제 그 응답을 받을 차례입니다.
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        //받은 json형식의 응답을 받아
                        JSONObject jsonObject = new JSONObject(response.toString());
                        String res = jsonObject.getString("approve_id");

                        if(res != null){
                            ANSWER[0] = res;
                            Tost(res);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //서버로 데이터 전달 및 응답 받기에 실패한 경우 아래 코드가 실행됩니다.
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    //Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
            //
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean BN_CheckString(String res){

        if(res.length() == 16){
            return true;
        }else
            return false;

    }

    public void Tost(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }

    public void LoginActivity(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    public void MainActivity(MyAccount myAccount){
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("myAccount",myAccount);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent,101);
    }


    @Override
    public void onBackPressed() {
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
        if (System.currentTimeMillis() > backKeyPressedTime + 1500) {
            backKeyPressedTime = System.currentTimeMillis();
          Tost("\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.");
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 1500) {
            //아래 3줄은 프로세스 종료
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

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