package com.qins.net.meta.core;

import com.qins.net.request.annotation.MethodPact;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;

@Getter
@Setter
public class MetaMethod {
    private Method method;
    private String mapping;
    private HashMap<String, MetaParameter> metaParameters;
    private HashMap<String, MetaParameter> metaSyncParameters;
    private MetaReturn metaReturn;
    private MethodPact methodPact;
}
