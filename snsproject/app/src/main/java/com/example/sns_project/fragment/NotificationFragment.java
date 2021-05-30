package com.example.sns_project.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.fcm.SendServer;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NotificationFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private MyAccount myAccount;
    private String location;
    private My_Utility my_utility;
    private PostControler postControler;
    private Button button;


    public NotificationFragment() {
        postControler = new PostControler();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        button = view.findViewById(R.id.noti);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendServer sendServer = new SendServer();
                sendServer.Send("제발요..","제발..",getActivity());

            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    public void Toast(String str){
        Toast.makeText(getActivity(),str,Toast.LENGTH_SHORT).show();
    }

}