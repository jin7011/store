package com.example.sns_project.data;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.sns_project.info.PostInfo;

public class LiveData_PostInfo  extends ViewModel {

    MutableLiveData<PostInfo> Mutable_PostInfo;

    public MutableLiveData<PostInfo> get (){
        if(Mutable_PostInfo == null){
            Mutable_PostInfo = new MutableLiveData<>();
        }
        return Mutable_PostInfo;
    }
}