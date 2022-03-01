package com.qins.net.core.entity;

import com.qins.net.meta.annotation.Meta;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@ToString
@Getter
@Setter
@Accessors(chain = true)
public class ResponseMeta {
    @Meta
    private String protocol = "Sync-Response-1.0";
    @Meta
    private Object result;
    @Meta
    private String exception;
    @Meta
    private Object instance;
    @Meta
    private Map<String,Object> params;
    @Meta
    private Map<String,Object> references;

    public ResponseMeta(){

    }
    public ResponseMeta(String exception){
        this.exception = exception;
        System.out.println(exception);
    }

    public ResponseMeta(Exception e) {
        try {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            exception = sw.toString();
            System.out.println(exception);
        }
        catch (Exception e2) {
            exception = "WriteExceptionError";
        }
    }

    public ResponseMeta(Object instance,Map<String,Object> params, Object result,Map<String,Object> references) {
        this.result = result;
        this.instance = instance;
        this.params = params;
        this.references = references;
    }
}
