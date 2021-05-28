package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationInfo implements Parcelable {

    private String Docid;
    private String location;
    private String contents;
    private Long createdAt;

    public NotificationInfo(){}

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

}
