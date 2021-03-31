package com.example.sns_project.data;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sns_project.info.PostInfo;

import java.util.ArrayList;

public class LiveData_PostList extends ViewModel {

    private MutableLiveData<ArrayList<PostInfo>> Mutable_postList = new MutableLiveData<>();

    public MutableLiveData<ArrayList<PostInfo>> get (){
        if(Mutable_postList == null){
            Mutable_postList = new MutableLiveData<>();
        }
        return Mutable_postList;
    }

}
