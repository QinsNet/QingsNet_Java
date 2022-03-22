package com.qins.net.meta.core;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ReferencesContext {
    Map<Integer,Object> serializeNames = new HashMap<>();
    Map<Integer,Object> deserializeNames = new HashMap<>();
    Map<Object,Object> serializeObjects = new HashMap<>();
    Map<Object,Object> deserializeObjects = new HashMap<>();
    Map<String, Object> serializePools = new HashMap<>();
    Map<String, Object> deserializePools;
}
