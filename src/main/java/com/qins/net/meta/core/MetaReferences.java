package com.qins.net.meta.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MetaReferences {
    Map<Object,String> serializeNames = new HashMap<>();
    Map<Object,String> deserializeNames = new HashMap<>();
    Map<String,Object> serializeObjects = new HashMap<>();
    Map<String,Object> deserializeObjects = new HashMap<>();
    Map<String,BaseClass> basesClass = new HashMap<>();
}
