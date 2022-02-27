package com.qins.net.core.exception;

import com.qins.net.meta.core.MetaClass;

public class NotFoundNodeException extends Exception{
    public NotFoundNodeException(String instance,String method){
        super(String.format("%s实体%s方法未匹配到远程节点", instance,method));
    }
    public NotFoundNodeException() {
        super();
    }

    public NotFoundNodeException(String message) {
        super(message);
    }

    public NotFoundNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundNodeException(Throwable cause) {
        super(cause);
    }

    protected NotFoundNodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
