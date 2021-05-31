package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyAccount implements Parcelable {

    private String id;
    private String nickname;
    private String image;
    private String location;
    private String store;
    private String phone;
    private String businessNum;
    private ArrayList<String> RoomKeys;
    private String token;

    public MyAccount(){}

    public MyAccount(String id, String nickname, String image, String location, String store, String phone, String businessNum,ArrayList<String> RoomKeys,String token) {
        this.id = id;
        this.nickname = nickname;
        this.image = image;
        this.location = location;
        this.store = store;
        this.phone = phone;
        this.businessNum = businessNum;
        this.RoomKeys = RoomKeys;
        this.token = token;
    }

    protected MyAccount(Parcel in) {
        id = in.readString();
        nickname = in.readString();
        image = in.readString();
        location = in.readString();
        store = in.readString();
        phone = in.readString();
        businessNum = in.readString();
        RoomKeys = in.createStringArrayList();
        token = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nickname);
        dest.writeString(image);
        dest.writeString(location);
        dest.writeString(store);
        dest.writeString(phone);
        dest.writeString(businessNum);
        dest.writeStringList(RoomKeys);
        dest.writeString(token);
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
        docData.put("phone",phone);
        docData.put("businessNum",businessNum);
        docData.put("RoomKeys", RoomKeys);
        docData.put("token",token);
        return  docData;
    }

    public String getStore() {
        return store;
    }
    public void setStore(String store) {
        this.store = store;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
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

    public ArrayList<String> getRoomKeys() {
        return RoomKeys;
    }

    public void setRoomKeys(ArrayList<String> roomKeys) {
        RoomKeys = roomKeys;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}

