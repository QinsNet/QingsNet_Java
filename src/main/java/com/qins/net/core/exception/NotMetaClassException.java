package com.qins.net.core.exception;

public class NotMetaClassException extends Exception{
    public NotMetaClassException(Object instance){
        super(instance.getClass().getName() + "未标记@Meta注解");
    }
}
