package com.example.sns_project.data;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sns_project.info.MyAccount;

public class LiveData_MyData_Main extends ViewModel {

    MutableLiveData<MyAccount> myAccountMutableLiveData;

    public MutableLiveData<MyAccount> get (){
        if(myAccountMutableLiveData == null){
            myAccountMutableLiveData = new MutableLiveData<>();
        }
        return myAccountMutableLiveData;
    }
}
