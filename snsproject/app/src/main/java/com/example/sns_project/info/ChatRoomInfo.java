package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class ChatRoomInfo implements Parcelable {

    private String user1;
    private String user2;
    private String user1_id;
    private String user2_id;
    private Long user1_OutDate;
    private Long user2_OutDate;
    private int user1_count;
    private int user2_count;
    private String key;

    private Long latestDate;
    private String latestMessage;

    public ChatRoomInfo(){}

    public ChatRoomInfo(String User1,
                        String User1_id,
                        Long User1_OutDate,
                        int user1_count,
                        String User2,
                        String User2_id,
                        Long User2_OutDate,
                        int user2_count,
                        String key){
        this.user1 = User1;
        this.user2 = User2;
        this.user1_id = User1_id;
        this.user2_id = User2_id;
        this.user1_OutDate = User1_OutDate;
        this.user2_OutDate = User2_OutDate;
        this.user1_count = user1_count;
        this.user2_count = user2_count;
//        this.latestDate = latestDate;
        this.key = key;
    }

    public ChatRoomInfo(ChatRoomInfo p){
        this.user1 = p.getUser1();
        this.user2 = p.getUser2();
        this.user1_id = p.getUser1_id();
        this.user2_id = p.getUser2_id();
        this.user1_OutDate = p.getUser1_OutDate();
        this.user2_OutDate = p.getUser2_OutDate();
        this.user1_count = p.getUser1_count();
        this.user2_count = p.getUser2_count();
        this.latestDate = p.getLatestDate();
        this.key = p.getKey();
    }

    public ChatRoomInfo(Map<String,Object> map){
        this.user1 = (String)map.get("user1");
        this.user2 = (String)map.get("user2");
        this.user1_id = (String)map.get("user1_id");
        this.user2_id = (String)map.get("user2_id");
        this.user1_OutDate = (Long)map.get("user1_OutDate");
        this.user2_OutDate = (Long)map.get("user2_OutDate");
        this.latestDate = (Long)map.get("createdAt");
        this.key =  (String)map.get("key");
    }

    public Map<String,Object> Get_ChatRoomInfo(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("User1", user1);
        docData.put("User2", user2);
        docData.put("User1_id", user1_id);
        docData.put("User2_id", user2_id);
        docData.put("User1_OutDate", user1_OutDate);
        docData.put("User2_OutDate", user2_OutDate);
        docData.put("createdAt", latestDate);
        docData.put("key", key);

        return docData;
    }

    public int getUser1_count() {
        return user1_count;
    }
    public void setUser1_count(int user1_count) {
        this.user1_count = user1_count;
    }
    public int getUser2_count() {
        return user2_count;
    }
    public void setUser2_count(int user2_count) {
        this.user2_count = user2_count;
    }
    public Long getLatestDate() {
        return latestDate;
    }
    public void setLatestDate(Long latestDate) {
        this.latestDate = latestDate;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getUser1() {
        return user1;
    }
    public void setUser1(String user1) {
        this.user1 = user1;
    }
    public String getUser2() {
        return user2;
    }
    public void setUser2(String user2) {
        this.user2 = user2;
    }
    public String getUser1_id() {
        return user1_id;
    }
    public void setUser1_id(String user1_id) {
        this.user1_id = user1_id;
    }
    public String getUser2_id() {
        return user2_id;
    }
    public void setUser2_id(String user2_id) {
        this.user2_id = user2_id;
    }
    public Long getUser1_OutDate() {
        return user1_OutDate;
    }
    public void setUser1_OutDate(Long user1_OutDate) {
        this.user1_OutDate = user1_OutDate;
    }
    public Long getUser2_OutDate() {
        return user2_OutDate;
    }
    public void setUser2_OutDate(Long user2_OutDate) {
        this.user2_OutDate = user2_OutDate;
    }
    public String getLatestMessage() {
        return latestMessage;
    }
    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    protected ChatRoomInfo(Parcel in) {
        user1 = in.readString();
        user2 = in.readString();
        user1_id = in.readString();
        user2_id = in.readString();
        user1_OutDate = in.readLong();
        user2_OutDate = in.readLong();
        user1_count = in.readInt();
        user2_count = in.readInt();
        latestDate = in.readLong();
        latestMessage = in.readString();
        key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user1);
        dest.writeString(user2);
        dest.writeString(user1_id);
        dest.writeString(user2_id);
        dest.writeLong(user1_OutDate);
        dest.writeLong(user2_OutDate);
        dest.writeInt(user1_count);
        dest.writeInt(user2_count);
        dest.writeLong(latestDate);
        dest.writeString(latestMessage);
        dest.writeString(key);
    }

    public static final Creator<ChatRoomInfo> CREATOR = new Creator<ChatRoomInfo>() {
        @Override
        public ChatRoomInfo createFromParcel(Parcel in) {
            return new ChatRoomInfo(in);
        }

        @Override
        public ChatRoomInfo[] newArray(int size) {
            return new ChatRoomInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
