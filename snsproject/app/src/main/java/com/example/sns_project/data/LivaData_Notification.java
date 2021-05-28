package com.example.sns_project.data;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.sns_project.info.NotificationInfo;
import java.util.ArrayList;

public class LivaData_Notification extends ViewModel {
    MutableLiveData<ArrayList<NotificationInfo>> Notis;

    public MutableLiveData<ArrayList<NotificationInfo>> get(){
        if(Notis == null){
            Notis = new MutableLiveData<>();
        }
        return Notis;
    }
}
