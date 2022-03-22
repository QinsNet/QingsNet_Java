package com.qins.net.core.aop.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class AfterEventContext extends EventContext{
    private Object result;

    public AfterEventContext(Map<String, Object> parameters, Method method, Object result) {
        super(parameters, method);
        this.result = result;
    }
}
