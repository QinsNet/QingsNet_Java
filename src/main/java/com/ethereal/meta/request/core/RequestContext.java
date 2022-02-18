package com.ethereal.meta.request.core;

import com.ethereal.meta.core.entity.NodeAddress;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;

@Getter
@Setter
public class RequestContext {
    private ResponseMeta responseMeta;
    private RequestMeta requestMeta;
    private Object instance;
    private Method method;
    private HashMap<String,Object> params;
    private NodeAddress remote;
    private boolean isVoid = false;
}
