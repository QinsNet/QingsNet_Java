package com.qins.net.core.entity;

import com.qins.net.meta.annotation.field.Sync;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@ToString
@Getter
@Setter
@Accessors(chain = true)
public class ResponseMeta {
    @Sync
    private String protocol = "Sync-Response-1.0";
    @Sync
    private Object result;
    @Sync
    private String exception;
    @Sync
    private Map<String,Object> instance;
    @Sync
    private Map<String,Object> params;
    @Sync
    private Map<String,Object> references;

    public ResponseMeta(){

    }
    public ResponseMeta(String exception){
        this.exception = exception;
    }

    public ResponseMeta(Exception e) {
        try {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            exception = sw.toString();
        }
        catch (Exception e2) {
            exception = "WriteExceptionError";
        }
    }

    public ResponseMeta(Map<String,Object> instance,Map<String,Object> params, Object result,Map<String,Object> references) {
        this.result = result;
        this.instance = instance;
        this.params = params;
        this.references = references;
    }
}
