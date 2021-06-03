package com.example.sns_project.info;

public class Declaration {

    private String Docid;
    private String userid;
    private String content;

    public Declaration(String docid, String userid, String content) {
        Docid = docid;
        this.userid = userid;
        this.content = content;
    }

    public String getDocid() {
        return Docid;
    }

    public void setDocid(String docid) {
        Docid = docid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}


