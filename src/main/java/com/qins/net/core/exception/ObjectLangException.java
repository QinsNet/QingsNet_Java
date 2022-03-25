package com.qins.net.core.exception;

public class ObjectLangException extends Exception{
    public ObjectLangException() {
        super();
    }

    public ObjectLangException(String message) {
        super(message);
    }

    public ObjectLangException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectLangException(Throwable cause) {
        super(cause);
    }

    protected ObjectLangException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
