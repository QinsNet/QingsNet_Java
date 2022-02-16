package com.ethereal.meta.service.core;

import com.ethereal.meta.core.entity.NodeAddress;
import com.ethereal.meta.core.entity.RequestMeta;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;

@Getter
@Setter
public class ServiceContext {
    private Object instance;
    private RequestMeta requestMeta;
    private HashMap<String,Object> params;
    private LinkedList<String> mappings;
    private NodeAddress local;
}
