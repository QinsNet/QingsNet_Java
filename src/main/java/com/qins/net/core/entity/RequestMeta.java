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
    @Meta
    private String protocol = "Sync-Request-1.0";
    @Meta
    private String mapping;
    @Meta
    private Map<String,String> params;
    @Meta
    private Map<String,String> references;
    @Meta
    private String instance = "";
}
