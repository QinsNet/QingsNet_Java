package com.ethereal.meta.core.entity;

import com.ethereal.meta.meta.Meta;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.HashMap;

@ToString
@Getter
@Setter
public class RequestMeta {
    private ResponseMeta result;
    private String protocol = "ER-1.0-Request";
    private String mapping;
    private HashMap<String,String> params;
    private String id;
    private Method method;
    private Class<? extends Meta> metaClass;
}
