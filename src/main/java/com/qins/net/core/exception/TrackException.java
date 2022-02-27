package com.qins.net.core.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TrackException extends Exception {
    public enum ExceptionCode { Initialize, Runtime, NotFoundMeta,NotMetaClass,NotFoundRequest, NotFoundMetaParameter,NotFoundParameter,NotFoundType,NewInstanceError,ResponseException}
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
