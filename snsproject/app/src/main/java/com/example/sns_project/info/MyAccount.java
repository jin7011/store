package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class MyAccount implements Parcelable {

    private String id;
    private String nickname;
    private String image;
    private String location;
    private String store;
    private String businessNum;
    private String token;
    private Boolean noti;

    public MyAccount(){}

    public MyAccount(String id, String nickname, String image, String location, String store, String businessNum,String token,Boolean noti) {
        this.id = id;
        this.nickname = nickname;
        this.image = image;
        this.location = location;
        this.store = store;
        this.businessNum = businessNum;
        this.token = token;
        this.noti = noti;
    }

    protected MyAccount(Parcel in) {
        id = in.readString();
        nickname = in.readString();
        image = in.readString();
        location = in.readString();
        store = in.readString();
        businessNum = in.readString();
        token = in.readString();
        noti = in.readBoolean();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nickname);
        dest.writeString(image);
        dest.writeString(location);
        dest.writeString(store);
        dest.writeString(businessNum);
        dest.writeString(token);
        dest.writeBoolean(noti);
    }

    public static final Creator<MyAccount> CREATOR = new Creator<MyAccount>() {
        @Override
        public MyAccount createFromParcel(Parcel in) {
            return new MyAccount(in);
        }
        @Override
        public MyAccount[] newArray(int size) {
            return new MyAccount[size];
        }
    };

    public Map<String, Object> getMap(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("id",id);
        docData.put("nickname",nickname);
        docData.put("image",image);
        docData.put("location",location);
        docData.put("store",store);
        docData.put("businessNum",businessNum);
        docData.put("token",token);
        docData.put("noti", noti);
        return  docData;
    }

    public String getStore() {
        return store;
    }
    public void setStore(String store) {
        this.store = store;
    }
    public String getBusinessNum() {
        return businessNum;
    }
    public void setBusinessNum(String businessNum) {
        this.businessNum = businessNum;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public Boolean getNoti() {
        return noti;
    }
    public void setNoti(Boolean noti) {
        this.noti = noti;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}

