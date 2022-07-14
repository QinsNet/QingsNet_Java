package com.qins.net.service.core;

import com.qins.net.core.lang.serialize.SerializeLang;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ServiceReferences {
    private Map<String, SerializeLang> serializeLang = new HashMap<>();
    private Map<Object, String> Ids = new HashMap<>();
    private Map<String,Object> serializeObjectsPool = new HashMap<>();
    private Map<String,Object> serializeDataPool = new HashMap<>();
    private Map<String,Object> deserializeObjectsPool = new HashMap<>();
    private Map<String,Object> deserializeDataPool;
}
