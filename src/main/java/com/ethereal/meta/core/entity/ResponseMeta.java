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
    private String mapping;
    @Expose
    private String meta;
    @Expose
    private Object instance;
    public ResponseMeta(){

    }
    public ResponseMeta(RequestMeta requestMeta, Error error) {
        this.mapping = requestMeta.getMapping();
        this.error = error;
        this.id = requestMeta.getId();
    }
    public ResponseMeta(RequestMeta requestMeta,String result) {
        this.result = result;
        this.mapping = requestMeta.getMapping();
        this.id = requestMeta.getId();
    }
}
