package com.ethereal.net.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class TrackException extends Exception {
    public enum ErrorCode {Core, Runtime, NotEthereal}
    private ErrorCode errorCode;
    private Object sender;
    public TrackException(ErrorCode errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }
    public TrackException(ErrorCode errorCode, String message,Object sender)
    {
        super(message);
        this.errorCode = errorCode;
        this.sender = sender;
    }
}
