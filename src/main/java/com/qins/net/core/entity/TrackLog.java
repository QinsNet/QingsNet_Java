package com.qins.net.core.entity;

import com.qins.net.meta.annotation.serialize.Sync;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Getter
@Setter
@Accessors(chain = true)
public class TrackLog {

    public enum LogCode { Core, Runtime }
    @Sync
    private String message;
    @Sync
    private LogCode code;
    @Sync
    private Object sender;
    public TrackLog(LogCode code,String message) {
        this.message = message;
        this.code = code;
    }
    public TrackLog(LogCode code,String message,Object sender) {
        this.message = message;
        this.code = code;
        this.sender = sender;
    }
}
