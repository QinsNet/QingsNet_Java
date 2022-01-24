package com.ethereal.meta.core.entity;

import com.google.gson.annotations.Expose;
import lombok.*;

@ToString
@Getter
@Setter
public class ResponseMeta {
    @Expose
    private final String type = "ER-1.0-Response";
    @Expose
    private String result = null;
    @Expose
    private Error error = null;
    @Expose
    private String id = null;

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
