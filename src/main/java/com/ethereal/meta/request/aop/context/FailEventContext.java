package com.ethereal.meta.request.aop.context;

import java.lang.reflect.Method;
import java.util.HashMap;

public class FailEventContext extends RequestContext {
    private Error error;

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public FailEventContext(HashMap<String, Object> parameters, Method method, Error error) {
        super(parameters, method);
        this.error = error;
    }
}
