package com.ethereal.meta.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class TrackException extends Exception {
    public enum ExceptionCode { Initialize, Runtime, NotFoundMeta,NotFoundRequest,NotFoundType,NewInstanceError,ResponseException}
    private ExceptionCode exceptionCode;
    public TrackException(ExceptionCode exceptionCode, String message)
    {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    @Override
    public String toString() {
        return "TrackException{" +
                "exceptionCode=" + exceptionCode +
                ",message=" + getMessage() +
                '}';
    }
}
