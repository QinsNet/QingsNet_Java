package com.qins.net.core.exception;

public class NotFoundInstanceFieldException extends Exception{
    public NotFoundInstanceFieldException(String instance, String method){
        super(String.format("%s实体%s方法未匹配到远程节点", instance,method));
    }
    public NotFoundInstanceFieldException() {
        super();
    }

    public NotFoundInstanceFieldException(String message) {
        super(message);
    }

    public NotFoundInstanceFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundInstanceFieldException(Throwable cause) {
        super(cause);
    }

    protected NotFoundInstanceFieldException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
