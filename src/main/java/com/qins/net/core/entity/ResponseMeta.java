package com.qins.net.core.entity;

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
    private String protocol = "Sync-Response-1.0";
    private String result;
    private String exception;
    private String instance;
    private Map<String,String> params;

    public ResponseMeta(){

    }
    public ResponseMeta(String exception){
        this.exception = exception;
    }

    public ResponseMeta(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            exception = sw.toString();
            System.out.println(exception);
        }
        catch (Exception e2) {
            exception = "ErrorInfoFromException";
        }
    }

    public ResponseMeta(String instance,Map<String,String> params, String result) {
        this.result = result;
        this.instance = instance;
        this.params = params;
    }
}
