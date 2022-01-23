package com.ethereal.net.core.entity;

import com.ethereal.net.node.core.Node;
import com.ethereal.net.service.core.Service;
import com.google.gson.annotations.Expose;
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
    private Service service;
    private Node node;
    private Method method;
}
