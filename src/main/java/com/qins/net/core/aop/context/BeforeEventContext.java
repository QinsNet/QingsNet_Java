package com.qins.net.core.aop.context;

import java.lang.reflect.Method;
import java.util.HashMap;

public class BeforeEventContext extends EventContext{
    public BeforeEventContext(HashMap<String, Object> parameters, Method method) {
        super(parameters, method);
    }
}
