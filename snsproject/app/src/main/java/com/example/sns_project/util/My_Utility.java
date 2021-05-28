package com.example.sns_project.util;

import android.app.Activity;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.sns_project.Adapter.ChatRoomAdapter;
import com.example.sns_project.Adapter.LetterAdapter;
import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.PostInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.example.sns_project.util.Named.GRID;
import static com.example.sns_project.util.Named.HORIZEN;
import static com.example.sns_project.util.Named.VERTICAL;

/**
use for recyclerview_init() and then
you can use PostControler with this for ~ by jin
**/

public class My_Utility {

    private Activity activity;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

    public My_Utility(Activity activity, RecyclerView recyclerView, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.adapter = adapter;
    }

    public void RecyclerInit(int orientation) {

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);

        if (orientation == VERTICAL) {
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
        } else if (orientation == HORIZEN) {
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);
        } else if (orientation == GRID) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 3);
            recyclerView.setLayoutManager(gridLayoutManager);
        }

        layoutManager.setItemPrefetchEnabled(true); //렌더링 퍼포먼스 향상
//        postAdapter.setHasStableIds(true); 이걸쓰면 게시물 시간이 재사용되서 리셋이 안되는 이슈가 발생
//        postAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY); //스크롤 저장하는건데 필요없어짐

        if (adapter instanceof PostAdapter) {
            ((PostAdapter) adapter).setLinearLayoutManager(layoutManager);
            ((PostAdapter) adapter).setRecyclerView(recyclerView);
        }

        if (adapter instanceof LetterAdapter) {
            ((LetterAdapter) adapter).setLinearLayoutManager(layoutManager);
            ((LetterAdapter) adapter).setRecyclerView(recyclerView);
        }

        recyclerView.setAdapter(adapter);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

//        if(activity.isDestroyed()){
//            adapter = null;
//            recyclerView = null;
//        }

    }

    public void Toast(String str) {
        Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();
    }

    public Activity getActivity() {
        return activity;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return adapter;
    }

    public static class Pair {
        Object result;
        int isSuccess;
        String location,Docid;

        public Pair() {
        }
        public Pair(Object result, int isSuccess) {
            this.isSuccess = isSuccess;
            this.result = result;
        }

        public Pair(String loaction,String Docid){
            this.location = loaction;
            this.Docid = Docid;
        }
        public Object getResult() {
            return result;
        }
        public void setResult(Object result) {
            this.result = result;
        }
        public int getIsSuccess() {
            return isSuccess;
        }
        public void setIsSuccess(int isSuccess) {
            this.isSuccess = isSuccess;
        }
        public String getLocation() {
            return location;
        }
        public void setLocation(String location) {
            this.location = location;
        }
        public String getDocid() {
            return Docid;
        }
        public void setDocid(String docid) {
            Docid = docid;
        }
    }

    public static class IDX_Pair {
        int result_1;
        int result_2;
        public IDX_Pair() {
        }
        public IDX_Pair(int result_1, int result_2) {
            this.result_1 = result_1;
            this.result_2 = result_2;
        }
        public int getResult_1() {
            return result_1;
        }
        public void setResult_1(int result_1) {
            this.result_1 = result_1;
        }
        public int getResult_2() {
            return result_2;
        }
        public void setResult_2(int result_2) {
            this.result_2 = result_2;
        }
    }

    public static HashMap<String, ChatRoomInfo> Sort_Map_ByValue(Map<String, ChatRoomInfo> map) {
        List<Map.Entry<String, ChatRoomInfo>> entries = new LinkedList<>(map.entrySet());
        entries.sort((o1, o2) -> o1.getValue().getLatestDate().compareTo(o2.getValue().getLatestDate()));

        HashMap<String, ChatRoomInfo> result = new HashMap<>();
        for (Map.Entry<String, ChatRoomInfo> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static HashMap<String, ChatRoomInfo> List_To_Map(ArrayList<ChatRoomInfo> rooms){

        HashMap<String, ChatRoomInfo> map = new HashMap<>();

        for(ChatRoomInfo room : rooms){
            map.put(room.getKey(),room);
        }

        return map;
    }

    public static ArrayList<ChatRoomInfo> Map_to_List(HashMap<String, ChatRoomInfo> map){

        ArrayList<ChatRoomInfo> rooms = new ArrayList<>();

        for(Map.Entry e : map.entrySet()){
            rooms.add((ChatRoomInfo)e.getValue());
        }

        rooms.sort(new Comparator<ChatRoomInfo>() {
            @Override
            public int compare(ChatRoomInfo o1, ChatRoomInfo o2) {
                return o2.getLatestDate().compareTo(o1.getLatestDate());
            }
        });

        return rooms;
    }
}
