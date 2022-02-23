package com.qins.net.request.aop.context;

import java.lang.reflect.Method;
import java.util.HashMap;

public class SuccessEventContext extends RequestContext {
    private Object remoteResult;

    public Object getRemoteResult() {
        return remoteResult;
    }

    public void setRemoteResult(Object remoteResult) {
        this.remoteResult = remoteResult;
    }

    public SuccessEventContext(HashMap<String, Object> parameters, Method method,Object remoteResult) {
        super(parameters, method);
        this.remoteResult = remoteResult;
    }
}
