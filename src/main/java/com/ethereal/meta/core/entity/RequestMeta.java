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
    private String protocol = "Meta-Request-1.0";
    private String mapping;
    private HashMap<String,String> params;
    private String id;
    private String meta;
}
