package com.qins.net.request.aop.context;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SuccessEventContext extends RequestContext {
    private Object remoteResult;
    public SuccessEventContext(Map<String, Object> parameters, Method method, Object remoteResult) {
        super(parameters, method);
        this.remoteResult = remoteResult;
    }
}
