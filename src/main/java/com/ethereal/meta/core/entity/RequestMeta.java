package com.ethereal.meta.core.entity;

import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.node.core.Node;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

@ToString
@Getter
@Setter
public class RequestMeta {
    private String protocol = "Meta-Request-1.0";
    private String mapping;
    private Map<String,String> params;
    private String meta = "";
    private String host;
    private String port;
}
