package com.example.sns_project.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.Adapter.ChatRoomAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_ChatRooms;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.util.My_Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.sns_project.util.My_Utility.List_To_Map;
import static com.example.sns_project.util.My_Utility.Map_to_List;
import static com.example.sns_project.util.My_Utility.Sort_Map_ByValue;
import static com.example.sns_project.util.Named.VERTICAL;

public class ChatRoomFragment extends Fragment {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    PostControler postControler;
    My_Utility my_utility;
    RecyclerView recyclerView;
    ChatRoomAdapter adapter;
    LiveData_ChatRooms liveData_chatRooms;
    private Observer<ArrayList<ChatRoomInfo>> RoomKeys_Observer;

    public ChatRoomFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);}

    @Override
    public void onDestroy() {
        super.onDestroy();
        liveData_chatRooms.get().removeObserver(RoomKeys_Observer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatroom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RoomKeys_Observer = new Observer<ArrayList<ChatRoomInfo>>() {
            @Override
            public void onChanged(@NonNull ArrayList<ChatRoomInfo> ChatRooms) {
                adapter.ChatRoomInfo_DiffUtile(ChatRooms);
            }
        };

        RecyclerInit(view);
        liveData_chatRooms = new ViewModelProvider(getActivity()).get(LiveData_ChatRooms.class);
        liveData_chatRooms.get().observeForever(RoomKeys_Observer);
        Set_RoomListener();

    }

    private void RecyclerInit(View view) {//저장된 db에서 내용을 뽑아오는 로직
        postControler = new PostControler();
        recyclerView = view.findViewById(R.id.ChatRoom_RecyclerView);
        adapter = new ChatRoomAdapter(getActivity(),postControler);
        my_utility = new My_Utility(getActivity(), recyclerView, adapter);
        my_utility.RecyclerInit(VERTICAL);
    }

    private void Set_RoomListener(){

        postControler.Set_Listener_Room(user.getUid(), new PostControler.Listener_Room() {
            @Override
            public void onAdded(ChatRoomInfo room) {
                HashMap<String,ChatRoomInfo> newmap = List_To_Map(liveData_chatRooms.get().getValue() == null ? new ArrayList<>() : liveData_chatRooms.get().getValue());
                newmap.put(room.getKey(),room);
                liveData_chatRooms.get().setValue(Map_to_List(newmap));
            }
            @Override
            public void onModified(ChatRoomInfo room) {
                HashMap<String,ChatRoomInfo> newmap = List_To_Map(liveData_chatRooms.get().getValue() == null ? new ArrayList<>() : liveData_chatRooms.get().getValue());
                newmap.put(room.getKey(),room);
                liveData_chatRooms.get().setValue(Map_to_List(newmap));

                for(int x=0; x<liveData_chatRooms.get().getValue().size(); x++){
                    Log.d("zkk13",liveData_chatRooms.get().getValue().get(x).getLatestMessage());
                }
            }
            @Override
            public void onDeleted(ChatRoomInfo room) {
                HashMap<String,ChatRoomInfo> newmap = List_To_Map(liveData_chatRooms.get().getValue() == null ? new ArrayList<>() : liveData_chatRooms.get().getValue());
                newmap.remove(room.getKey());
                liveData_chatRooms.get().setValue(Map_to_List(newmap));
            }
        });

    }

}
