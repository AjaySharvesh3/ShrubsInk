package com.shrubsink.everylifeismatter.model;

import java.util.Date;

public class Notification extends com.shrubsink.everylifeismatter.model.NotificationId {

    String message, from, user_name;
    Date timestamp;

    Notification() {}

    public Notification(String message, String from, String user_name, Date timestamp) {
        this.message = message;
        this.from = from;
        this.user_name = user_name;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
