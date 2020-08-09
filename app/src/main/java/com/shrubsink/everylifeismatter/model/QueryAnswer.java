package com.shrubsink.everylifeismatter.model;

import java.util.Date;

public class QueryAnswer {

    private String answer, user_id, userName, thumbnail;
    private Date timestamp;

    public QueryAnswer() {}

    public QueryAnswer(String answer, String user_id, String userName, String thumbnail, Date timestamp) {
        this.answer = answer;
        this.user_id = user_id;
        this.userName = userName;
        this.thumbnail = thumbnail;
        this.timestamp = timestamp;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
