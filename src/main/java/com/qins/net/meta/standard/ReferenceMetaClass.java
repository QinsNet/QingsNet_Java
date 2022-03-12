package com.qins.net.meta.standard;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.MetaReferences;
import com.qins.net.util.SerializeUtil;
import javafx.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

public abstract class ReferenceMetaClass extends StandardMetaClass {
    static Random random = new Random();
    public ReferenceMetaClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(instanceClass);
    }
    @Override
    public Object serialize(Object instance, MetaReferences references, Map<String, Object> pools) throws IllegalAccessException {
        if(instance == null)return null;
        String name;
        if(references.getSerializeNames().containsKey(instance)){
            return references.getSerializeNames().get(instance);
        }
        if(references.getDeserializeNames().containsKey(instance)){
            name = references.getDeserializeNames().get(instance);
        }
        else {
            name = instance.toString();
            while (references.getSerializeNames().containsKey(name)){
                name = instance.toString() + random.nextInt();
            }
        }
        references.getSerializeNames().put(instance,name);
        references.getSerializeObjects().put(name,instance);
        references.getBasesClass().put(name,this);
        pools.put(name,super.serialize(instance,references,pools));
        return new JsonPrimitive(name);
    }


    @Override
    public Object deserialize(Object rawJsonElement, MetaReferences references, Map<String, Object> pools) throws InstantiationException, IllegalAccessException {
        if(rawJsonElement == null)return null;
        String name = ((JsonPrimitive)rawJsonElement).getAsString();
        JsonElement jsonElement = (JsonElement) pools.get(name);
        Object instance;
        if(references.getDeserializeObjects().containsKey(name)){
            return references.getDeserializeObjects().get(name);
        }
        if(references.getSerializeObjects().containsKey(name)){
            instance = references.getSerializeObjects().get(name);
        }
        else if(jsonElement.isJsonPrimitive()){
            instance = SerializeUtil.gson.fromJson(jsonElement,instanceClass);
        }
        else {
            instance = proxyClass.newInstance();
        }
        references.getDeserializeNames().put(instance,name);
        references.getDeserializeObjects().put(name,instance);
        references.getBasesClass().put(name,this);
        super.deserialize(new Pair<>(instance,jsonElement),references,pools);
        return instance;
    }
}
