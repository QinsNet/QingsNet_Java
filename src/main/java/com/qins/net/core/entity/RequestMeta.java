package com.qins.net.core.entity;

import com.qins.net.meta.annotation.field.Sync;
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
    @Sync
    private String protocol = "Sync-Request-1.0";
    @Sync
    private String mapping;
    @Sync
    private Map<String,String> params;
    @Sync
    private Map<String,Object> references;
    @Sync
    private String instance;
}
