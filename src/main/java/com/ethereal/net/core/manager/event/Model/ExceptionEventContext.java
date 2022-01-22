package com.ethereal.net.core.manager.event.Model;

import java.lang.reflect.Method;
import java.util.HashMap;

public class ExceptionEventContext extends EventContext{
    private Exception exception;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public ExceptionEventContext(HashMap<String, Object> parameters, Method method, Exception exception) {
        super(parameters, method);
        this.exception = exception;
    }
}
