package com.example.sns_project.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.sns_project.Activities.MainActivity;
import com.example.sns_project.Activities.Password_resetActivity;
import com.example.sns_project.Activities.PopupActivity;
import com.example.sns_project.R;
import com.example.sns_project.databinding.FragmentProfileBinding;
import com.example.sns_project.info.MyAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private MyAccount myAccount;
    private FragmentProfileBinding binding;
    private Context context;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false);
        View view = binding.getRoot();
        context = container.getContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        myAccount = (MyAccount)bundle.getParcelable("Myaccount");
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        binding.setMyaccount(myAccount);
        binding.setProfileFragment(this);
    }

    public void withdraw(View view){

        AlertDialog.Builder oDialog =
                new AlertDialog.Builder(
                getActivity(),android.R.style.Theme_DeviceDefault_Dialog);

        oDialog.setMessage("회원탈퇴를 하시겠습니까?\n사용자의 정보가 모두 삭제되지만,\n'작성글'과 '댓글'의 내용은 남아있게 됩니다.")
                .setPositiveButton("예", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                oDialog.setMessage("진짜..?").setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        db.collection("USER").document(myAccount.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                logout();
                                            }
                                        }
                                    });
                                }
                                else
                                    Log.d("asdzzzxcccaa","스토리지 제거 실패");
                            }
                        });
                    }

                }).setNeutralButton("무르기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast("굿");
                    }
                }).show();

            }
        }).setNeutralButton("아니오", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
            }
        }).show();
    }

    public void change_password(View view){
        Intent intent = new Intent(context,Password_resetActivity.class);
        startActivity(intent);
    }

    public void change_location(View view){
        Intent intent = new Intent(getActivity(), PopupActivity.class);
        intent.putExtra("myAccount",myAccount);
        startActivityForResult(intent,101);
    }

    public void logout_dialog(View view){

        AlertDialog.Builder oDialog = new AlertDialog.Builder(getActivity(),
                android.R.style.Theme_DeviceDefault_Dialog);

        oDialog.setMessage("로그아웃 하시겠습니까?").setPositiveButton("예", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                logout();
            }
        }).setNeutralButton("아니오", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                })
                .show();
    }

    public void logout(){
        ((MainActivity)getActivity()).logout();
//        this.activity.logout();
        Toast("로그아웃 되었습니다.");
    }

    public void Toast(String str){
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }
}