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
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

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
            public void onChanged(ArrayList<ChatRoomInfo> ChatRooms) {
                adapter.ChatRoomInfo_DiffUtile(ChatRooms);
            }
        };
        liveData_chatRooms = new ViewModelProvider(getActivity()).get(LiveData_ChatRooms.class);
        liveData_chatRooms.get().observeForever(RoomKeys_Observer);

        RecyclerInit(view);
        Set_RoomListener();
    }

    private void RecyclerInit(View view) {//저장된 db에서 내용을 뽑아오는 로직
        recyclerView = view.findViewById(R.id.ChatRoom_RecyclerView);
        adapter = new ChatRoomAdapter(getActivity(), new ChatRoomAdapter.Listener_NewMessage() {
            @Override
            public void onNewMessage(ChatRoomInfo room, int position) {
                ArrayList<ChatRoomInfo> rooms = liveData_chatRooms.get().getValue() == null ? new ArrayList<>() : new ArrayList<>(liveData_chatRooms.get().getValue());
                rooms.remove(position);
                rooms.add(0,room);
                liveData_chatRooms.get().setValue(rooms);
            }
        });

        my_utility = new My_Utility(getActivity(), recyclerView, adapter);
        my_utility.RecyclerInit(VERTICAL);
        postControler = new PostControler();
    }

    private void Set_RoomListener(){
        Log.d("qpqpq","시작은한거지???");
        postControler.Set_RoomKeys_Listener_From_User(user.getUid(), new PostControler.Listener_Get_RoomKeys() {
            @Override
            public void GetRoomKeys(ArrayList<String> rooms) { //리스너를 붙이는건데 처음에 있는 목록을 다 들고옴
                ArrayList<ChatRoomInfo> newrooms = liveData_chatRooms.get().getValue() == null ? new ArrayList<>() : new ArrayList<>(liveData_chatRooms.get().getValue());
                Log.d("rkwudha","rooms: "+newrooms.size());

                Set_Value(rooms,newrooms,0);
            }
        });
    }

    public void Set_Value(ArrayList<String> roomkeys,ArrayList<ChatRoomInfo> newrooms,int idx){
        Log.d("qpqpq","size: "+newrooms.size());

        String key = roomkeys.get(idx);

        postControler.Get_RoomInfo_From_DB(key, new PostControler.Listener_Get_Room() {
            @Override
            public void onGetRoom(ChatRoomInfo room) {
                newrooms.add(room);
                if(roomkeys.size() == newrooms.size()){
                    Log.d("rkwudha","Keys: "+roomkeys.size() + " rooms: "+newrooms.size());
                    liveData_chatRooms.get().setValue(newrooms);
                }else{
                    Set_Value(roomkeys,newrooms,idx+1);
                }
            }
        });
    }
}
