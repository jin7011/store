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
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.Adapter.NotificationAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.fcm.SendServer;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.info.NotificationInfo;
import com.example.sns_project.util.My_Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;

import static com.example.sns_project.util.Named.VERTICAL;

public class NotificationFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private MyAccount myAccount;
    private String location;
    private My_Utility my_utility;
    private PostControler postControler;
    private NotificationAdapter adapter;
    private RecyclerView recyclerView;

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
        RecyclerView_init(view);
        super.onViewCreated(view, savedInstanceState);
    }

    public void Toast(String str){
        Toast.makeText(getActivity(),str,Toast.LENGTH_SHORT).show();
    }

    public void RecyclerView_init(View view){
        //todo 노티리스너를 달아줘야함
        FirebaseFirestore.getInstance().collection("USER").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notification").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<NotificationInfo> notis = new ArrayList<>();
                for(Iterator<QueryDocumentSnapshot> it = task.getResult().iterator(); it.hasNext();){
                    notis.add(it.next().toObject(NotificationInfo.class));
                }
                adapter = new NotificationAdapter(notis);
                recyclerView = view.findViewById(R.id.Notification_RecyclerView);
                my_utility = new My_Utility(getActivity(),recyclerView,adapter);
                my_utility.RecyclerInit(VERTICAL);
            }
        });


    }

}