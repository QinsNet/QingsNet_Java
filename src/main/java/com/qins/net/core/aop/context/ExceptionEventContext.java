package com.qins.net.core.aop.context;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ExceptionEventContext extends EventContext{
    private Exception exception;


    public ExceptionEventContext(Map<String, Object> parameters, Method method, Exception exception) {
        super(parameters, method);
        this.exception = exception;
    }
}
