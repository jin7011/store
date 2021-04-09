package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

public class CommentInfo implements Parcelable {

    private String key;
    private String contents;
    private String publisher;
    private Date createdAt;
    private String id;
    private int good;
    private ArrayList<RecommentInfo> recomments;

    public CommentInfo(String contents, String publisher, Date createdAt, String id, int good,ArrayList<RecommentInfo> recomments,String key) {
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.id = id;
        this.good = good;
        this.recomments = recomments;
        this.key = key;
    }

    protected CommentInfo(Parcel in) {
        contents = in.readString();
        publisher = in.readString();
        createdAt = new Date(in.readLong());
        id = in.readString();
        good = in.readInt();
        recomments = in.createTypedArrayList(RecommentInfo.CREATOR);
        key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contents);
        dest.writeString(publisher);
        dest.writeLong(createdAt.getTime());
        dest.writeString(id);
        dest.writeInt(good);
        dest.writeTypedList(recomments);
        dest.writeString(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CommentInfo> CREATOR = new Creator<CommentInfo>() {
        @Override
        public CommentInfo createFromParcel(Parcel in) {
            return new CommentInfo(in);
        }

        @Override
        public CommentInfo[] newArray(int size) {
            return new CommentInfo[size];
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
    public ArrayList<RecommentInfo> getRecomments() {
        return recomments;
    }
    public void setRecomments(ArrayList<RecommentInfo> recomments) {
        this.recomments = recomments;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

}
