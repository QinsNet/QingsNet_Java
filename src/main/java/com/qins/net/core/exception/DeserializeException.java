package com.qins.net.core.exception;

public class DeserializeException extends Exception{
    public DeserializeException(String msg){
        super(msg);
    }
    public DeserializeException(Throwable e) {
        super(e);
    }
}
