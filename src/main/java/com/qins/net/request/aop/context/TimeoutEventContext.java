package com.qins.net.request.aop.context;

import java.lang.reflect.Method;
import java.util.HashMap;

public class TimeoutEventContext extends RequestContext {

    public TimeoutEventContext(HashMap<String, Object> parameters, Method method) {
        super(parameters, method);
    }
}
