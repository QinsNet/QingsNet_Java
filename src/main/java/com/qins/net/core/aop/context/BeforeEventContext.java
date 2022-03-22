package com.qins.net.core.aop.context;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BeforeEventContext extends EventContext{
    public BeforeEventContext(Map<String, Object> parameters, Method method) {
        super(parameters, method);
    }
}
