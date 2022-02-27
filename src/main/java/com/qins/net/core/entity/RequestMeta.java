package com.qins.net.core.entity;

import com.qins.net.meta.annotation.Meta;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

@ToString
@Getter
@Setter
@Accessors(chain = true)
public class RequestMeta {
    private String protocol = "Sync-Request-1.0";
    private String mapping;
    private Map<String,String> params;
    private String instance = "";
    private String host;
    private String port;
}
