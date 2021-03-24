package com.example.sns_project.Info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostInfo implements Serializable {

        private String title;
        private String contents;
        private ArrayList<String> formats;
        private String publisher;
        private Date createdAt;
        private String id;
        private String docid;
        private int good;
        private int comment;

        public PostInfo(String id, String publisher, String title, String contents, ArrayList<String> formats, Date createdAt ){ //글쓰기에서 쓰임(파일포함)
            this.title = title;
            this.contents = contents;
            this.formats = formats;
            this.publisher = publisher;
            this.createdAt = createdAt;
            this.id = id;
        }

    public PostInfo(String id, String publisher, String title, String contents,Date createdAt ){ //글쓰기에서 쓰임(파일미포함)
        this.title = title;
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.id = id;
    }

    public PostInfo(String id, String publisher, String title, String contents,ArrayList<String> formats,Date createdAt,String docid,int good,int comment ){
            //게시판 업로드용(format포함)
        this.title = title;
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.formats = formats;
        this.id = id;
        this.docid = docid;
        this.good = good;
        this.comment = comment;
    }

    public PostInfo(String id, String publisher, String title, String contents,Date createdAt,String docid,int good,int comment ){
            //게시판 업로드용(format미포함) 비동기 db저장하면서 리사이클러뷰에 넣는데 이상하게 오류나기땜에 따로 놨음
        this.title = title;
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.id = id;
        this.docid = docid;
        this.good = good;
        this.comment = comment;
    }


    public Map<String, Object> getPostInfo(){
            Map<String, Object> docData = new HashMap<>();
            docData.put("title",title);
            docData.put("contents",contents);
            docData.put("formats",formats);
            docData.put("publisher",publisher);
            docData.put("createdAt",createdAt);
            return  docData;
        }

    public String getTitle(){
        return this.title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getContents(){
        return this.contents;
    }
    public void setContents(String contents){
        this.contents = contents;
    }
    public ArrayList<String> getFormats(){
        return this.formats;
    }
    public void setFormats(ArrayList<String> formats){
        this.formats = formats;
    }
    public String getPublisher(){
        return this.publisher;
    }
    public void setPublisher(String publisher){
        this.publisher = publisher;
    }
    public Date getCreatedAt(){
        return this.createdAt;
    }
    public void setCreatedAt(Date createdAt){
        this.createdAt = createdAt;
    }
    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getDocid() {
        return docid;
    }
    public void setDocid(String docid) {
        this.docid = docid;
    }

    public int getGood() {
        return good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }
}