package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;

public class LetterInfo implements Parcelable {

    private String sender_nick;
    private String sender_id;
    private String reciever_nick;
    private String reciever_id;
    private String contents;
    private Date createdAt;

    public LetterInfo(String sender_nick, String sender_id, String reciever_nick, String reciever_id, String contents, Date createdAt) {
        this.sender_nick = sender_nick;
        this.sender_id = sender_id;
        this.reciever_nick = reciever_nick;
        this.reciever_id = reciever_id;
        this.contents = contents;
        this.createdAt = createdAt;
    }

    public LetterInfo(LetterInfo p){
        this.sender_nick = p.getSender_nick();
        this.sender_id = p.getSender_id();
        this.reciever_nick = p.getReciever_nick();
        this.reciever_id = p.getReciever_id();
        this.contents = p.getContents();
        this.createdAt = p.getCreatedAt();
    }

    public String getContents() {
        return contents;
    }
    public void setContents(String contents) {
        this.contents = contents;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
    public String getReciever_nick() {
        return reciever_nick;
    }
    public void setReciever_nick(String reciever_nick) {
        this.reciever_nick = reciever_nick;
    }
    public String getReciever_id() {
        return reciever_id;
    }
    public void setReciever_id(String reciever_id) {
        this.reciever_id = reciever_id;
    }

    protected LetterInfo(Parcel in) {
        sender_nick = in.readString();
        sender_id = in.readString();
        reciever_nick = in.readString();
        reciever_id = in.readString();
        contents = in.readString();
        createdAt = new Date(in.readLong());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender_nick);
        dest.writeString(sender_id);
        dest.writeString(reciever_nick);
        dest.writeString(reciever_id);
        dest.writeString(contents);
        dest.writeLong(createdAt.getTime());
    }

    public static final Creator<LetterInfo> CREATOR = new Creator<LetterInfo>() {
        @Override
        public LetterInfo createFromParcel(Parcel in) {
            return new LetterInfo(in);
        }

        @Override
        public LetterInfo[] newArray(int size) {
            return new LetterInfo[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

}
