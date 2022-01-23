package com.ethereal.net.core.manager.aop.context;

import java.lang.reflect.Method;
import java.util.HashMap;

public class AfterEventContext extends EventContext{
    private Object result;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public AfterEventContext(HashMap<String, Object> parameters, Method method, Object result) {
        super(parameters, method);
        this.result = result;
    }
}
