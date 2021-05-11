package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomInfo implements Parcelable {

    private String User1;
    private String User2;
    private String User1_id;
    private String User2_id;
    private Date User1_OutDate;
    private Date User2_OutDate;
    private Date createdAt;
    private ArrayList<LetterInfo> letters;
    private String key;

    public ChatRoomInfo(String User1,
                        String User1_id,
                        Date User1_OutDate,
                        String User2,
                        String User2_id,
                        Date User2_OutDate,
                        Date createdAt, ArrayList<LetterInfo> letters, String key){
        this.User1 = User1;
        this.User2 = User2;
        this.User1_id = User1_id;
        this.User2_id = User2_id;
        this.User1_OutDate = User1_OutDate;
        this.User2_OutDate = User2_OutDate;
        this.createdAt = createdAt;
        this.letters = letters;
        this.key = key;
    }

    public ChatRoomInfo(ChatRoomInfo p){
        this.User1 = p.getUser1();
        this.User2 = p.getUser2();
        this.User1_id = p.getUser1_id();
        this.User2_id = p.getUser2_id();
        this.User1_OutDate = p.getUser1_OutDate();
        this.User2_OutDate = p.getUser2_OutDate();
        this.createdAt = p.getCreatedAt();
        this.letters = new ArrayList<>(p.getLetters());
        this.key = p.getKey();
    }

    public Map<String,Object> Get_ChatRoomInfo(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("User1", User1);
        docData.put("User2", User2);
        docData.put("User1_id", User1_id);
        docData.put("User2_id", User2_id);
        docData.put("User1_OutDate", User1_OutDate);
        docData.put("User2_OutDate", User2_OutDate);
        docData.put("createdAt", createdAt);
        docData.put("letters", letters);
        docData.put("key", key);

        return docData;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public ArrayList<LetterInfo> getLetters() {
        return letters;
    }
    public void setLetters(ArrayList<LetterInfo> letters) {
        this.letters = letters;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getUser1() {
        return User1;
    }
    public void setUser1(String user1) {
        User1 = user1;
    }
    public String getUser2() {
        return User2;
    }
    public void setUser2(String user2) {
        User2 = user2;
    }
    public String getUser1_id() {
        return User1_id;
    }
    public void setUser1_id(String user1_id) {
        User1_id = user1_id;
    }
    public String getUser2_id() {
        return User2_id;
    }
    public void setUser2_id(String user2_id) {
        User2_id = user2_id;
    }
    public Date getUser1_OutDate() {
        return User1_OutDate;
    }
    public void setUser1_OutDate(Date user1_OutDate) {
        User1_OutDate = user1_OutDate;
    }
    public Date getUser2_OutDate() {
        return User2_OutDate;
    }
    public void setUser2_OutDate(Date user2_OutDate) {
        User2_OutDate = user2_OutDate;
    }

    protected ChatRoomInfo(Parcel in) {
        User1 = in.readString();
        User2 = in.readString();
        User1_id = in.readString();
        User2_id = in.readString();
        User1_OutDate = new Date(in.readLong());
        User2_OutDate = new Date(in.readLong());
        createdAt = new Date(in.readLong());
        key = in.readString();
        letters = in.createTypedArrayList(LetterInfo.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(User1);
        dest.writeString(User2);
        dest.writeString(User1_id);
        dest.writeString(User2_id);
        dest.writeLong(User1_OutDate.getTime());
        dest.writeLong(User2_OutDate.getTime());
        dest.writeLong(createdAt.getTime());
        dest.writeString(key);
        dest.writeTypedList(letters);
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
