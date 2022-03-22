package com.qins.net.meta.core;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ReferencesContext {
    private Map<Integer,String> serializeReferences = new HashMap<>();
    private Map<String,Object> serializeObjects = new HashMap<>();
    private Map<Integer,String> deserializeReferences = new HashMap<>();
    private Map<String,Object> deserializeObjects = new HashMap<>();
    private Map<String,Object> serializePool = new HashMap<>();
    private Map<String,Object> deserializePool;
}
