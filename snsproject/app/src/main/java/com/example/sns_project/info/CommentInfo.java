package com.example.sns_project.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommentInfo implements Parcelable {

    private String key;
    private String contents;
    private String publisher;
    private Date createdAt;
    private String id;
    private int good;
    private HashMap<String, Integer> good_user;
    private ArrayList<RecommentInfo> recomments;
    private String DateFormate_for_layout;
    private String How_Long;

    public CommentInfo(){}

    public CommentInfo(String contents, String publisher, Date createdAt, String id, int good,HashMap<String, Integer> good_user,ArrayList<RecommentInfo> recomments,String key) {
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.id = id;
        this.good = good;
        this.recomments = recomments;
        this.key = key;
        this.good_user = good_user; //생성자 파라미터에는 없지만 처음 생성할 때는 비어있는게 정상이니 그대로 생성.
    }

    public CommentInfo(CommentInfo p) {
        //깊은 복사 전용
        this.contents = p.getContents();
        this.publisher = p.getPublisher();
        this.createdAt = p.getCreatedAt();
        this.id = p.getId();
        this.good = p.getGood();
        this.DateFormate_for_layout = p.getDateFormate_for_layout();
        this.good_user = new HashMap<String,Integer>(p.getGood_user());
        this.recomments = deepCopy_RecommentInfo(p.getRecomments());
        this.key = p.getKey();
    }

    protected CommentInfo(Parcel in) {
        contents = in.readString();
        publisher = in.readString();
        createdAt = new Date(in.readLong());
        id = in.readString();
        good = in.readInt();
        recomments = in.createTypedArrayList(RecommentInfo.CREATOR);
        key = in.readString();
        DateFormate_for_layout = in.readString();
        How_Long = in.readString();

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
        dest.writeTypedList(recomments);
        dest.writeString(key);
        dest.writeString(DateFormate_for_layout);
        dest.writeString(How_Long);

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
    public String getDateFormate_for_layout() {
        return DateFormate_for_layout;
    }
    public void setDateFormate_for_layout(String dateFormate_for_layout) {
        DateFormate_for_layout = dateFormate_for_layout;
    }
    public String getHow_Long() {
        return How_Long;
    }
    public void setHow_Long(String how_Long) {
        How_Long = how_Long;
    }
    public HashMap<String, Integer> getGood_user() {
        return good_user;
    }
    public void setGood_user(HashMap<String, Integer> good_user) {
        this.good_user = good_user;
    }

    public ArrayList<RecommentInfo> deepCopy_RecommentInfo(ArrayList<RecommentInfo> oldone){

        ArrayList<RecommentInfo> newone = new ArrayList<>();

        for(int x=0; x<oldone.size(); x++) {
            if(oldone.get(x)==null)
                continue;
            newone.add(new RecommentInfo(oldone.get(x)));
        }
        return newone;
    }

}
