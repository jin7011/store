package com.example.sns_project.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.sns_project.Activities.LoginActivity;
import com.example.sns_project.Activities.MainActivity;
import com.example.sns_project.R;
import com.example.sns_project.databinding.FragmentProfileBinding;
import com.example.sns_project.info.MyAccount;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private MyAccount myAccount;
    private FragmentProfileBinding binding;
    private MainActivity activity;
    private Context context;

    public ProfileFragment(MainActivity activity) {
        this.activity = activity;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        binding.setMyaccount(myAccount);
        binding.setProfileFragment(this);
    }

    public void logout_dialog(View view){

        AlertDialog.Builder oDialog = new AlertDialog.Builder(activity,
                android.R.style.Theme_DeviceDefault_Light_Dialog);

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
        this.activity.logout();
        Toast("로그아웃 되었습니다.");
    }

    public void Toast(String str){
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }
}