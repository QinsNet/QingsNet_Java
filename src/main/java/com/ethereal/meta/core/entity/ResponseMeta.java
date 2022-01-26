package com.ethereal.meta.core.entity;

import com.google.gson.annotations.Expose;
import lombok.*;

@ToString
@Getter
@Setter
public class ResponseMeta {
    @Expose
    private String protocol = "Meta-Response-1.0";
    @Expose
    private String result;
    @Expose
    private Error error;
    @Expose
    private String id;
    @Expose
    private String mapping;
    @Expose String meta;
    public ResponseMeta(){

    }
    public ResponseMeta(String result, String id, Error error) {
        this.result = result;
        this.error = error;
        this.id = id;
    }

    public ResponseMeta(String id, Error error) {
        this.error = error;
        this.id = id;
    }

}
