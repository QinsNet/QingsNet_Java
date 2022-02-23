package com.qins.net.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@ToString
@Getter
@Setter
public class RequestMeta {
    private String protocol = "Meta-Request-1.0";
    private String mapping;
    private Map<String,String> params;
    private String instance = "";
    private String host;
    private String port;
}
