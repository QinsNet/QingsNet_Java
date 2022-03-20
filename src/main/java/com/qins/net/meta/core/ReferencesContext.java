package com.qins.net.meta.core;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ReferencesContext {
    Map<Object,String> serializeNames = new HashMap<>();
    Map<Object,String> deserializeNames = new HashMap<>();
    Map<String,Object> serializeObjects = new HashMap<>();
    Map<String,Object> deserializeObjects = new HashMap<>();
    Map<String, Object> serializePools = new HashMap<>();
    Map<String, Object> deserializePools;
}
