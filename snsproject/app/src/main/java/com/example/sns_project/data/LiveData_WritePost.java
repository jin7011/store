package com.example.sns_project.data;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class LiveData_WritePost extends ViewModel {

     MutableLiveData<ArrayList<Uri>> Mutable_WritePost_images;

    public MutableLiveData<ArrayList<Uri>> get (){
        if(Mutable_WritePost_images == null){
            Mutable_WritePost_images = new MutableLiveData<>();
        }
        return Mutable_WritePost_images;
    }

}