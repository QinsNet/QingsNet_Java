package com.ethereal.meta.request.aop.context;

import com.ethereal.meta.core.entity.Error;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;

@Getter
@Setter
public class ErrorEventContext extends RequestContext {
    private Error error;

    public ErrorEventContext(HashMap<String, Object> parameters, Method method, Error error) {
        super(parameters, method);
        this.error = error;
    }
}
