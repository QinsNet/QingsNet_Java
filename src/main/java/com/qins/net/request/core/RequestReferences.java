package com.qins.net.request.core;

import com.qins.net.core.lang.serialize.ObjectLang;
import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.MetaMethod;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class RequestReferences {
    private String serialize;
    private String deserialize;
    private Map<Integer,String> serializeReferences = new HashMap<>();
    private Map<Integer, SerializeLang> serializeLang = new HashMap<>();
    private Map<String,Object> serializeObjects = new HashMap<>();
    private Map<String,Object> deserializeObjects = new HashMap<>();
    private Map<String,Object> serializePool = new HashMap<>();
    private Map<String,Object> deserializePool;
    private BaseClass mainClass;
    private MetaMethod metaMethod;
}
