package com.example.sns_project.util;

import android.app.Activity;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.sns_project.Adapter.PostAdapter;
import com.example.sns_project.info.PostInfo;

import java.util.ArrayList;

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

    public My_Utility(Activity activity,RecyclerView recyclerView,RecyclerView.Adapter<RecyclerView.ViewHolder> adapter){
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.adapter = adapter;
    }

    public void RecyclerInit(int orientation) {

        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);

        if(orientation == VERTICAL) {
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
        }else if(orientation == HORIZEN) {
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);
        }else if(orientation == GRID){
            GridLayoutManager gridLayoutManager = new GridLayoutManager(activity,3);
            recyclerView.setLayoutManager(gridLayoutManager);
        }

        layoutManager.setItemPrefetchEnabled(true); //렌더링 퍼포먼스 향상
//        postAdapter.setHasStableIds(true); 이걸쓰면 게시물 시간이 재사용되서 리셋이 안되는 이슈가 발생
//        postAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY); //스크롤 저장하는건데 필요없어짐

        if(adapter instanceof PostAdapter) {
            ((PostAdapter) adapter).setLinearLayoutManager(layoutManager);
            ((PostAdapter) adapter).setRecyclerView(recyclerView);
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

    public void Tost(String str){
        Toast.makeText(activity,str,Toast.LENGTH_SHORT).show();
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
}
