package com.qins.net.service.core;

import com.google.gson.JsonPrimitive;
import com.qins.net.core.lang.serialize.SerializeLang;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ServiceReferences {
    private Map<Integer,String> serializeReferences = new HashMap<>();
    private Map<Integer,String> deserializeReferences = new HashMap<>();
    private Map<String, SerializeLang> serializeLang = new HashMap<>();
    private Map<String,Object> deserializeObjects = new HashMap<>();
    private Map<String,Object> serializePool = new HashMap<>();
    private Map<String,Object> deserializePool;
}
