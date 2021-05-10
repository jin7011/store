package com.example.sns_project.data;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sns_project.info.LetterInfo;

public class LiveData_Letters extends ViewModel {

    MutableLiveData<LetterInfo> Letters;

    public MutableLiveData<LetterInfo> get(){
        if(Letters == null){
            Letters = new MutableLiveData<>();
        }
        return Letters;
    }

}
