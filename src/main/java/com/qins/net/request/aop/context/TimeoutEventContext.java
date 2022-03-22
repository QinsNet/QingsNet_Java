package com.qins.net.request.aop.context;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TimeoutEventContext extends RequestContext {

    public TimeoutEventContext(Map<String, Object> parameters, Method method) {
        super(parameters, method);
    }
}
