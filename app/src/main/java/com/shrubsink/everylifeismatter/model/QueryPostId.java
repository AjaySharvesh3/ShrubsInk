package com.shrubsink.everylifeismatter.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class QueryPostId {

    @Exclude
    public String QueryPostId;

    public <T extends QueryPostId> T withId(@NonNull final String id) {
        this.QueryPostId = id;
        return (T) this;
    }

}
