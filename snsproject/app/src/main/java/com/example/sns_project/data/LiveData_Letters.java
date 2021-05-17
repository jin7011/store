package com.example.sns_project.data;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.LetterInfo;

import java.util.ArrayList;

public class LiveData_Letters extends ViewModel {

    MutableLiveData<ArrayList<LetterInfo>> Letters;

    public MutableLiveData<ArrayList<LetterInfo>> get(){
        if(Letters == null){
            Letters = new MutableLiveData<>();
        }
        return Letters;
    }

}
