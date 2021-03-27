package com.example.sns_project.info;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MyAccount implements Serializable {

    private String id;
    private String nickname;
    private String image;
    private String location;
    private String store;
    private String phone;
    private String businessNum;

    public MyAccount(String id, String nickname, String image, String location, String store, String phone, String businessNum) {
        this.id = id;
        this.nickname = nickname;
        this.image = image;
        this.location = location;
        this.store = store;
        this.phone = phone;
        this.businessNum = businessNum;
    }

    public Map<String, Object> getMap(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("id",id);
        docData.put("nickname",nickname);
        docData.put("image",image);
        docData.put("location",location);
        docData.put("store",store);
        docData.put("phone",phone);
        docData.put("businessNum",businessNum);
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

}
