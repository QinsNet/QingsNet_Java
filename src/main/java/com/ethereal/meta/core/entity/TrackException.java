package com.ethereal.meta.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class TrackException extends Exception {
    public enum ExceptionCode {Initialize, Runtime, NotFoundMeta,NotFoundRequest}
    private ExceptionCode exceptionCode;
    private Object sender;
    public TrackException(ExceptionCode exceptionCode, String message)
    {
        super(message);
        this.exceptionCode = exceptionCode;
    }
    public TrackException(ExceptionCode exceptionCode, String message, Object sender)
    {
        super(message);
        this.exceptionCode = exceptionCode;
        this.sender = sender;
    }
}
