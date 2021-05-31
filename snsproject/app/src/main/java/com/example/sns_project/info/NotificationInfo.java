package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationInfo implements Parcelable {

    private String type;
    private String Docid;
    private String location;
    private String contents;
    private Long createdAt;
    private String token;
    private String sender;


    public NotificationInfo(){}

    public NotificationInfo(String type,String token,String sender,String contents,String Docid,Long createdAt){
        this.type = type;
        this.token = token;
        this.sender = sender;
        this.contents = contents;
        this.Docid = Docid;
        this.createdAt = createdAt;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Docid);
        dest.writeString(location);
        dest.writeString(contents);
        if (createdAt == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(createdAt);
        }
    }

    protected NotificationInfo(Parcel in) {
        Docid = in.readString();
        location = in.readString();
        contents = in.readString();
        if (in.readByte() == 0) {
            createdAt = null;
        } else {
            createdAt = in.readLong();
        }
    }

    public static final Creator<NotificationInfo> CREATOR = new Creator<NotificationInfo>() {
        @Override
        public NotificationInfo createFromParcel(Parcel in) {
            return new NotificationInfo(in);
        }

        @Override
        public NotificationInfo[] newArray(int size) {
            return new NotificationInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public String getDocid() {
        return Docid;
    }
    public void setDocid(String docid) {
        Docid = docid;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getContents() {
        return contents;
    }
    public void setContents(String contents) {
        this.contents = contents;
    }
    public Long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
