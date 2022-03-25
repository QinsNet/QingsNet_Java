package com.qins.net.core.entity;

import com.qins.net.meta.annotation.serialize.Sync;
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
    private String result;
    @Sync
    private String exception;
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
}
