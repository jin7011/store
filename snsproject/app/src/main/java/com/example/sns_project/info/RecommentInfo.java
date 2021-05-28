package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.sns_project.CustomLibrary.PostControler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RecommentInfo implements Parcelable{

    private String contents;
    private String publisher;
    private Date createdAt;
    private String id;
    private int good;
    private HashMap<String, Integer> good_user;

    public RecommentInfo(){}

    public RecommentInfo(String contents, String publisher, Date createdAt, String id, int good,HashMap<String, Integer> good_user) {
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.id = id;
        this.good = good;
        this.good_user = good_user;
    }

    public RecommentInfo(RecommentInfo p) {
        this.contents = p.getContents();
        this.publisher = p.getPublisher();
        this.createdAt = p.getCreatedAt();
        this.id = p.getId();
        this.good = p.getGood();
        this.good_user = new HashMap<>(p.getGood_user());
    }

    protected RecommentInfo(Parcel in) {
        contents = in.readString();
        publisher = in.readString();
        createdAt = new Date(in.readLong());
        id = in.readString();
        good = in.readInt();

        int size = in.readInt();
        Log.d("vktmffjqmf2",""+size);
        if(size != 0) {
            good_user = new HashMap<>();
            for (int i = 0; i < size; i++) {
                good_user.put(in.readString(), in.readInt());
            }
        }else{
            good_user = new HashMap<>();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contents);
        dest.writeString(publisher);
        dest.writeLong(createdAt.getTime());
        dest.writeString(id);
        dest.writeInt(good);

        dest.writeInt(good_user.size());
        Log.d("vktmffjqmf",""+good_user.size());
        for(Map.Entry<String, Integer> entry : good_user.entrySet()) {
            String key = entry.getKey();
            int val = Integer.parseInt(String.valueOf(entry.getValue())); //해쉬값에 있는 int가 number형이라 에러났던건데 그거때문에 오래 삽질했음.
            dest.writeString(key);
            dest.writeInt(val);
        }
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
    public HashMap<String, Integer> getGood_user() {
        return good_user;
    }
    public void setGood_user(HashMap<String, Integer> good_user) {
        this.good_user = good_user;
    }
}

