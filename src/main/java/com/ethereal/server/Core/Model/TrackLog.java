package com.ethereal.server.Core.Model;

public class TrackLog {
    public enum LogCode { Core, Runtime }
    private String message;
    private LogCode code;
    private Object sender;

    public Object getSender() {
        return sender;
    }

    public void setSender(Object sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LogCode getCode() {
        return code;
    }

    public void setCode(LogCode code) {
        this.code = code;
    }

    public TrackLog(LogCode code, String message) {
        this.message = message;
        this.code = code;
    }
}
