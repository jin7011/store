package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;

public class RecommentInfo implements Parcelable{

    private String contents;
    private String publisher;
    private Date createdAt;
    private String id;
    private int good;

    public RecommentInfo(String contents, String publisher, Date createdAt, String id, int good) {
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.id = id;
        this.good = good;
    }

    protected RecommentInfo(Parcel in) {
        contents = in.readString();
        publisher = in.readString();
        createdAt = new Date(in.readLong());
        id = in.readString();
        good = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contents);
        dest.writeString(publisher);
        dest.writeLong(createdAt.getTime());
        dest.writeString(id);
        dest.writeInt(good);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RecommentInfo> CREATOR = new Creator<RecommentInfo>() {
        @Override
        public RecommentInfo createFromParcel(Parcel in) {
            return new RecommentInfo(in);
        }

        @Override
        public RecommentInfo[] newArray(int size) {
            return new RecommentInfo[size];
        }
    };

    public String getContents() {
        return contents;
    }
    public void setContents(String contents) {
        this.contents = contents;
    }
    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getGood() {
        return good;
    }
    public void setGood(int good) {
        this.good = good;
    }

}

