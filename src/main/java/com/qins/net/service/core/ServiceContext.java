package com.qins.net.service.core;

import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.meta.core.MetaMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;

@Getter
@Setter
@Accessors(chain = true)
public class ServiceContext {
    private Object instance;
    private RequestMeta requestMeta;
    private ResponseMeta responseMeta;
    private HashMap<String,Object> params;
    private String mapping;
    private ServiceReferences references;
    private MetaMethod metaMethod;
    private Object result;
}
