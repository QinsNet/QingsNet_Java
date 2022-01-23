package com.ethereal.net.request.aop.context;

import com.ethereal.net.core.manager.aop.context.EventContext;

import java.lang.reflect.Method;
import java.util.HashMap;

public class RequestContext extends EventContext {

    public RequestContext(HashMap<String, Object> parameters, Method method) {
        super(parameters, method);
    }
}
