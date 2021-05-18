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
//todo 지금 라이브에 roominfo를 넣어야하나 키를 넣어야하나 하다가 잠들어감 내일 생각해봐야함. 스트링으로하면 어쩌면 디프가 필요없을지도? 내용물에 대한 관리는 홀더에서 하게 될테니까.
    //todo 어댑터에서 홀더나 바인드에 각 채팅방의 리스너를 달아서 최신 글을 홀더바인딩하고 리스너에서 반응이 오는 것을 플래그먼트에서 리스너로 응답을 받고 디프, 이곳 플래그먼트에서는 라이브로 store를 리스닝
    private void RecyclerInit(View view) {//저장된 db에서 내용을 뽑아오는 로직

        recyclerView = view.findViewById(R.id.ChatRoom_RecyclerView);
        adapter = new ChatRoomAdapter(getActivity(), new ChatRoomAdapter.Listener_NewMessage() {
            @Override
            public void onNewMessage(ChatRoomInfo room, int position) {
                ArrayList<ChatRoomInfo> rooms = new ArrayList<>(liveData_chatRooms.get().getValue());
                rooms.remove(position);
                rooms.add(0,room);
                liveData_chatRooms.get().setValue(rooms);
            }
        });

        my_utility = new My_Utility(getActivity(), recyclerView, adapter);
        my_utility.RecyclerInit(VERTICAL);
        postControler = new PostControler();

    }

//    private void Get_Rooms(){
//        postControler.Get_Rooms_From_User(user.getUid(), new PostControler.Listener_Get_RoomKeys() {
//            @Override
//            public void GetRoomKeys(ArrayList<String> rooms) {
//                ArrayList<ChatRoomInfo> newrooms = new ArrayList<>();
//
//                for(String key : rooms) {
//                    postControler.Get_RoomInfo_From_DB(key, new PostControler.Listener_Get_Room() {
//                        @Override
//                        public void onGetRoom(ChatRoomInfo room) {
//                            newrooms.add(room);
//                            if(rooms.size() == newrooms.size()){
//                                Log.d("rkwudha","Keys: "+rooms.size() + " rooms: "+newrooms.size());
//                                liveData_chatRooms.get().setValue(newrooms);
//                                Set_RoomListener();
//                            }
//                        }
//                    });
//                }
//            }
//        });
//    }

    private void Set_RoomListener(){
        postControler.Set_RoomKeys_Listener_From_User(user.getUid(), new PostControler.Listener_Get_RoomKeys() {
            @Override
            public void GetRoomKeys(ArrayList<String> rooms) { //리스너를 붙이는건데 처음에 있는 목록을 다 들고옴
                ArrayList<ChatRoomInfo> newrooms = liveData_chatRooms.get().getValue() == null ? new ArrayList<>() : new ArrayList<>(liveData_chatRooms.get().getValue());

                for(String key : rooms) {
                    postControler.Get_RoomInfo_From_DB(key, new PostControler.Listener_Get_Room() {
                        @Override
                        public void onGetRoom(ChatRoomInfo room) {
                            newrooms.add(room);
                            if(rooms.size() == newrooms.size()){
                                Log.d("rkwudha","Keys: "+rooms.size() + " rooms: "+newrooms.size());
                                liveData_chatRooms.get().setValue(newrooms);
                            }
                        }
                    });
                }
            }
        });
    }
}