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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.Adapter.NotificationAdapter;
import com.example.sns_project.CustomLibrary.PostControler;
import com.example.sns_project.Listener.Listener_Recyclerview_Touch;
import com.example.sns_project.R;
import com.example.sns_project.data.LiveData_Notification;
import com.example.sns_project.info.NotificationInfo;
import com.example.sns_project.util.My_Utility;
import com.example.sns_project.util.Recyclerview_ItemTouch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.sns_project.util.My_Utility.List_To_Map_Noti;
import static com.example.sns_project.util.My_Utility.Map_to_List_Noti;
import static com.example.sns_project.util.My_Utility.Map_to_List_Room;
import static com.example.sns_project.util.Named.VERTICAL;

public class NotificationFragment extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private My_Utility my_utility;
    private PostControler postControler = new PostControler();
    private NotificationAdapter adapter;
    private RecyclerView recyclerView;
    private LiveData_Notification liveData_notis;
    private Observer<ArrayList<NotificationInfo>> Notis_Observer;

    public NotificationFragment() {
        postControler = new PostControler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        liveData_notis.get().removeObserver(Notis_Observer);
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
        super.onViewCreated(view, savedInstanceState);

        Notis_Observer = new Observer<ArrayList<NotificationInfo>>() {
            @Override
            public void onChanged(ArrayList<NotificationInfo> notificationInfos) {
                adapter.NotificationInfo_DiffUtil(notificationInfos);
                Log.d("asdz12",notificationInfos.size()+"");
            }
        };

        RecyclerView_init(view);
        liveData_notis = new ViewModelProvider(getActivity()).get(LiveData_Notification.class);
        liveData_notis.get().observeForever(Notis_Observer);
        Set_NotisListener();

    }

    private void Set_NotisListener() {
        postControler.Set_Listener_Noti(user.getUid(), new PostControler.Listener_Noti() {
            @Override
            public void onAdded(NotificationInfo noti) {
                HashMap<String,NotificationInfo> newmap = List_To_Map_Noti(liveData_notis.get().getValue() == null ? new ArrayList<>() : liveData_notis.get().getValue());
                newmap.put(noti.getDocid(),noti);
                liveData_notis.get().setValue(Map_to_List_Noti(newmap));
            }

            @Override
            public void onModified(NotificationInfo noti) {
                HashMap<String,NotificationInfo> newmap = List_To_Map_Noti(liveData_notis.get().getValue() == null ? new ArrayList<>() : liveData_notis.get().getValue());
                newmap.put(noti.getDocid(),noti);
                liveData_notis.get().setValue(Map_to_List_Noti(newmap));
            }

            @Override
            public void onDeleted(NotificationInfo noti) {
                HashMap<String,NotificationInfo> newmap = List_To_Map_Noti(liveData_notis.get().getValue() == null ? new ArrayList<>() : liveData_notis.get().getValue());
                newmap.remove(noti.getDocid());
                liveData_notis.get().setValue(Map_to_List_Noti(newmap));
            }
        });

    }

    public void RecyclerView_init(View view){
        //todo 노티리스너를 달아줘야함
        adapter = new NotificationAdapter();
        recyclerView = view.findViewById(R.id.Notification_RecyclerView);
        my_utility = new My_Utility(getActivity(),recyclerView,adapter);
        my_utility.RecyclerInit(VERTICAL);

        ItemTouchHelper helper = new ItemTouchHelper(new Recyclerview_ItemTouch(new Listener_Recyclerview_Touch() {
            @Override
            public void onItemSwipe(RecyclerView.ViewHolder viewHolder) {
                postControler.Delete_Noti(user.getUid(), liveData_notis.get().getValue().get(viewHolder.getAbsoluteAdapterPosition()).getDocid(), new PostControler.Listener_Delete_Noti() {
                    @Override
                    public void onComplete() {}
                });
            }
        }));
        helper.attachToRecyclerView(recyclerView);
    }
}