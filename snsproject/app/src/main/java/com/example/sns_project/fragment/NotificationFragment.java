package com.example.sns_project.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.example.sns_project.activity.MainActivity;
import com.example.sns_project.R;
import com.example.sns_project.databinding.FragmentNotificationBinding;
import com.example.sns_project.info.MyAccount;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private MyAccount myAccount;
    private String location;
    private MainActivity activity;
    private FragmentNotificationBinding binding;
    private Context context;
    private My_Utility my_utility;


    public NotificationFragment() {
        this.activity = (MainActivity) getActivity();
//        this.my_utility = new My_Utility(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_notification,container,false);
        View view = binding.getRoot();
        context = container.getContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        myAccount = (MyAccount)bundle.getParcelable("Myaccount");
        location = myAccount.getLocation();
        Toast(location);

        super.onViewCreated(view, savedInstanceState);
    }

    public void Search(String KeyWord){
//        db.collection(location).
    }

    public void Toast(String str){
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }

}