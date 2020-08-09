package com.shrubsink.everylifeismatter.model;

import java.util.Date;

public class QueryPost extends com.shrubsink.everylifeismatter.model.QueryPostId  {

    public String user_id, image_url, title, body, issue_location, tags, image_thumb;
    public Date timestamp;

    public QueryPost() {}

    public QueryPost(String user_id, String image_url, String title, String body, String issue_location, String tags, String image_thumb, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.title = title;
        this.body = body;
        this.issue_location = issue_location;
        this.tags = tags;
        this.image_thumb = image_thumb;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getIssue_location() {
        return issue_location;
    }

    public void setIssue_location(String issue_location) {
        this.issue_location = issue_location;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
