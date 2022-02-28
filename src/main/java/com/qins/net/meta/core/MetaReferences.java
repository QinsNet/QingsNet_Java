package com.qins.net.meta.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MetaReferences {
    Map<Object,String> oldNames = new HashMap<>();
    Map<Object,String> newNames = new HashMap<>();
    Map<String,Object> oldObjects = new HashMap<>();
    Map<String,Object> newObjects = new HashMap<>();
}
