package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Date;

public class ChatRoomInfo implements Parcelable {

    private String sender_nick;
    private String sender_id;
    private String receiver_nick;
    private String receiver_id;
    private Date createdAt;
    private ArrayList<LetterInfo> letters;
    private String key;

    public ChatRoomInfo(String sender_nick, String sender_id, String receiver_nick,
                        String receiver_id, Date createdAt, ArrayList<LetterInfo> letters, String key){
        this.sender_nick = sender_nick;
        this.sender_id = sender_id;
        this.receiver_nick = receiver_nick;
        this.receiver_id = receiver_id;
        this.createdAt = createdAt;
        this.letters = letters;
        this.key = key;
    }

    public ChatRoomInfo(ChatRoomInfo p){
        this.sender_nick = p.getSender_nick();
        this.sender_id = p.getSender_id();
        this.receiver_nick = p.getReceiver_nick();
        this.receiver_id = p.getReceiver_id();
        this.createdAt = p.getCreatedAt();
        this.letters = new ArrayList<>(p.getLetters());
        this.key = p.getKey();
    }

    public String getSender_nick() {
        return sender_nick;
    }
    public void setSender_nick(String sender_nick) {
        this.sender_nick = sender_nick;
    }
    public String getSender_id() {
        return sender_id;
    }
    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }
    public String getReceiver_nick() {
        return receiver_nick;
    }
    public void setReceiver_nick(String receiver_nick) {
        this.receiver_nick = receiver_nick;
    }
    public String getReceiver_id() {
        return receiver_id;
    }
    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
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

    protected ChatRoomInfo(Parcel in) {
        sender_nick = in.readString();
        sender_id = in.readString();
        receiver_nick = in.readString();
        receiver_id = in.readString();
        createdAt = new Date(in.readLong());
        key = in.readString();
        letters = in.createTypedArrayList(LetterInfo.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender_nick);
        dest.writeString(sender_id);
        dest.writeString(receiver_nick);
        dest.writeString(receiver_id);
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
