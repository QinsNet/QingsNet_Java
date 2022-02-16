package com.ethereal.meta.request.core;

import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.node.core.RemoteInfo;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;

@Getter
@Setter
public class RequestContext {
    private ResponseMeta result;
    private RequestMeta request;
    private Object instance;
    private Method method;
    private HashMap<String,Object> params;
    private RemoteInfo remoteInfo;
}
