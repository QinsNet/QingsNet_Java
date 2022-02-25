package com.qins.net.core.exception;

public class NewInstanceException extends Exception{
    public NewInstanceException() {
        super();
    }

    public NewInstanceException(String message) {
        super(message);
    }

    public NewInstanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NewInstanceException(Throwable cause) {
        super(cause);
    }

    protected NewInstanceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
