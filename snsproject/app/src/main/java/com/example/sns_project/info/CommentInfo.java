package com.example.sns_project.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class CommentInfo implements Serializable {

    private String contents;
    private String publisher;
    private Date createdAt;
    private String id;
    private int good;
    private ArrayList<CommentInfo> comments;

    public CommentInfo(String contents, String publisher, Date createdAt, String id, int good, ArrayList<CommentInfo> comments) {
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.id = id;
        this.good = good;
        this.comments = comments;
    }

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
    public ArrayList<CommentInfo> getComments() {
        return comments;
    }
    public void setComments(ArrayList<CommentInfo> comments) {
        this.comments = comments;
    }
}
