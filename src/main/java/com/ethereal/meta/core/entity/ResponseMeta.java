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
    private String instance;

    public ResponseMeta(){

    }
    public ResponseMeta(Error error) {
        this.error = error;
    }
    public ResponseMeta(String instance, String result) {
        this.result = result;
        this.instance = instance;
    }
}
