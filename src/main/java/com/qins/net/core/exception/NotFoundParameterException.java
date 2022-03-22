package com.qins.net.core.exception;

public class NotFoundParameterException extends Exception{
    public NotFoundParameterException(String instance, String method){
        super(String.format("%s实体%s方法未匹配到远程节点", instance,method));
    }
    public NotFoundParameterException() {
        super();
    }

    public NotFoundParameterException(String message) {
        super(message);
    }

    public NotFoundParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundParameterException(Throwable cause) {
        super(cause);
    }

    protected NotFoundParameterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
