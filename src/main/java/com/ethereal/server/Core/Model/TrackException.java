package com.ethereal.server.Core.Model;

public class TrackException extends Exception {
    public enum ErrorCode {Core, Runtime, NotEthereal}
    private Exception exception;
    private ErrorCode errorCode;

    private Object sender;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object getSender() {
        return sender;
    }

    public void setSender(Object sender) {
        this.sender = sender;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public TrackException(ErrorCode errorCode, String message)
    {
        super(message);
        this.exception = this;
        this.errorCode = errorCode;
    }
    public TrackException(Exception e)
    {
        super("外部库错误");
        this.exception = e;
        this.errorCode = ErrorCode.NotEthereal;
    }
}
