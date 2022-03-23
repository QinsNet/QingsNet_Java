package com.qins.net.request.core;

import com.qins.net.core.entity.NodeAddress;
import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.meta.core.MetaMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class RequestContext {
    private ResponseMeta responseMeta;
    private RequestMeta requestMeta;
    private Object instance;
    private MetaMethod metaMethod;
    private Map<String,Object> params;
    private RequestReferences references;
    private NodeAddress remote;
    private Object result;
    private boolean isVoid = false;
}
