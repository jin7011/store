package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
    private HashMap<String, Date> RoomsKey_Date;

    public MyAccount(String id, String nickname, String image, String location, String store, String phone, String businessNum,HashMap<String, Date> RoomsKey_Date) {
        this.id = id;
        this.nickname = nickname;
        this.image = image;
        this.location = location;
        this.store = store;
        this.phone = phone;
        this.businessNum = businessNum;
        this.RoomsKey_Date = RoomsKey_Date;
    }

    protected MyAccount(Parcel in) {
        id = in.readString();
        nickname = in.readString();
        image = in.readString();
        location = in.readString();
        store = in.readString();
        phone = in.readString();
        businessNum = in.readString();

        int size = in.readInt();
        if(size != 0) {
            RoomsKey_Date = new HashMap<>();
            for (int i = 0; i < size; i++) {
                RoomsKey_Date.put(in.readString(), new Date(in.readLong()));
            }
        }else{
            RoomsKey_Date = new HashMap<>();
        }
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

        dest.writeInt(RoomsKey_Date.size());
        for(Map.Entry<String, Date> entry : RoomsKey_Date.entrySet()) {
            String key = entry.getKey();
            Long val = entry.getValue().getTime(); //해쉬값에 있는 int가 number형이라 에러났던건데 그거때문에 오래 삽질했음.
            dest.writeString(key);
            dest.writeLong(val);
        }

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
        docData.put("RoomsKey_Date",RoomsKey_Date);
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
    public HashMap<String, Date> getRoomsKey_Date() {
        return RoomsKey_Date;
    }
    public void setRoomsKey_Date(HashMap<String, Date> roomsKey_Date) {
        RoomsKey_Date = roomsKey_Date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}

