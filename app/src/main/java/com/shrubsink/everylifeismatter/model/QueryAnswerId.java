package com.shrubsink.everylifeismatter.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class QueryAnswerId {

    @Exclude
    public String QueryAnswerId;

    public <T extends QueryAnswerId> T withId(@NonNull final String id) {
        this.QueryAnswerId = id;
        return (T) this;
    }

}
