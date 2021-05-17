package com.example.sns_project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.Adapter.ChatRoomAdapter;
import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_ChatRooms;
import com.example.sns_project.databinding.FragmentChatroomBinding;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.PostInfo;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static com.example.sns_project.util.Named.VERTICAL;

public class ChatRoomFragment extends Fragment {

    PostControler postControler;
    My_Utility my_utility;
    RecyclerView recyclerView;
    ChatRoomAdapter adapter;
    LiveData_ChatRooms liveData_chatRooms;
    private Observer<ArrayList<ChatRoomInfo>> Room_Observer;

    public ChatRoomFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);}

    @Override
    public void onDestroy() {
        super.onDestroy();
        liveData_chatRooms.get().removeObserver(Room_Observer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatroom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Room_Observer = new Observer<ArrayList<ChatRoomInfo>>() {
            @Override
            public void onChanged(ArrayList<ChatRoomInfo> ChatRooms) {

            }
        };

        liveData_chatRooms.get().observeForever(Room_Observer);
        RecyclerInit(view);
    }

    private void RecyclerInit(View view) {//저장된 db에서 내용을 뽑아오는 로직

        recyclerView = view.findViewById(R.id.ChatRoom_RecyclerView);
        adapter = new ChatRoomAdapter(getActivity());
        my_utility = new My_Utility(getActivity(),recyclerView,adapter);
        my_utility.RecyclerInit(VERTICAL);
        postControler = new PostControler();

    }
}