package com.qins.net.core.entity;

import lombok.*;

import java.util.Map;

@ToString
@Getter
@Setter
public class ResponseMeta {
    private String protocol = "Sync-Response-1.0";
    private String result;
    private String exception;
    private String instance;
    private Map<String,String> params;

    public ResponseMeta(){

    }

    public ResponseMeta(String exception) {
        this.exception = exception;
    }

    public ResponseMeta(String instance,Map<String,String> params, String result) {
        this.result = result;
        this.instance = instance;
        this.params = params;
    }
}
