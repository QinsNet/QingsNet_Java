package com.ethereal.server.Request.Event.Model;

import com.ethereal.server.Core.Manager.Event.Model.EventContext;

import java.lang.reflect.Method;
import java.util.HashMap;

public class RequestContext extends EventContext {

    public RequestContext(HashMap<String, Object> parameters, Method method) {
        super(parameters, method);
    }
}
