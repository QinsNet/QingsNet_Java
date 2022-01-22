package com.ethereal.net.core.manager.event.Model;

import java.lang.reflect.Method;
import java.util.HashMap;

public class EventContext {
    private Method method;
    private HashMap<String, Object> parameters;

    public EventContext(HashMap<String, Object> parameters,Method method) {
        this.method = method;
        this.parameters = parameters;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }
}
