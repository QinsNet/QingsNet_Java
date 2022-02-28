package com.qins.net.service.core;

import com.qins.net.core.entity.NodeAddress;
import com.qins.net.core.entity.RequestMeta;
import com.qins.net.meta.core.MetaReferences;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.LinkedList;

@Getter
@Setter
@Accessors(chain = true)
public class ServiceContext {
    private Object instance;
    private RequestMeta requestMeta;
    private HashMap<String,Object> params;
    private String mapping;
    private MetaReferences references;
}
