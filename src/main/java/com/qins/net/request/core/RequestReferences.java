package com.qins.net.request.core;

import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.MetaMethod;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class RequestReferences {
    private Map<String, SerializeLang> deserializeLang = new HashMap<>();
    private Map<String,Object> serializeObjectsPool = new HashMap<>();//Object对应ID时，Object的Hash会变动，会造成重复创建，所以采用ID对应Object。
    private Map<String,Object> serializeDataPool = new HashMap<>();
    private Map<String,Object> deserializeObjectsPool = new HashMap<>();
    private Map<String,Object> deserializeDataPool;
    private BaseClass mainClass;
    private MetaMethod metaMethod;
}
