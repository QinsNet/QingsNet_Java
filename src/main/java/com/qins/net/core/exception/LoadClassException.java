package com.qins.net.core.exception;

public class LoadClassException extends Exception{
    public LoadClassException() {
        super();
    }

    public LoadClassException(String message) {
        super(message);
    }

    public LoadClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadClassException(Throwable cause) {
        super(cause);
    }

    protected LoadClassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
