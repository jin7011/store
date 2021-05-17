package com.example.sns_project.data;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sns_project.info.ChatRoomInfo;
import com.example.sns_project.info.LetterInfo;
import java.util.ArrayList;

public class LiveData_ChatRooms  extends ViewModel {

    MutableLiveData<ArrayList<ChatRoomInfo>> Rooms;

    public MutableLiveData<ArrayList<ChatRoomInfo>> get(){
        if(Rooms == null){
            Rooms = new MutableLiveData<>();
        }
        return Rooms;
    }

}