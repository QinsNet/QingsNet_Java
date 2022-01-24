package com.ethereal.meta.request.aop.context;

import com.ethereal.meta.core.aop.context.EventContext;

import java.lang.reflect.Method;
import java.util.HashMap;

public class RequestContext extends EventContext {

    public RequestContext(HashMap<String, Object> parameters, Method method) {
        super(parameters, method);
    }
}
