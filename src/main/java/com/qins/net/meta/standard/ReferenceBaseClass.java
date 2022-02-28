package com.qins.net.meta.standard;

import com.google.gson.*;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.MetaReferences;
import com.qins.net.util.SerializeUtil;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

public class ReferenceBaseClass extends StandardBaseClass {
    static Random random = new Random();
    public ReferenceBaseClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(instanceClass);
    }
    @Override
    public Object serializeAsObject(Object instance, MetaReferences references, Map<String, String> pools) throws IllegalAccessException {
        if(instance == null)return null;
        String name;
        if(references.getOldNames().containsKey(instance)){
            if(references.getNewNames().containsKey(instance))return new JsonPrimitive(references.getNewNames().get(instance));
            name = references.getOldNames().get(instance);
            references.getNewNames().put(instance,name);
            references.getNewObjects().put(name,instance);
            pools.put(name,SerializeUtil.gson.toJson(super.serializeAsObject(instance,references,pools)));
        }
        else {
            name = instance.toString();
            while (references.getOldNames().containsKey(name)){
                name = instance.toString() + random.nextInt();
            }
            references.getOldNames().put(instance,name);
            references.getOldObjects().put(name,instance);
            pools.put(name,SerializeUtil.gson.toJson(super.serializeAsObject(instance,references,pools)));
        }
        return new JsonPrimitive(name);
    }
    @Override
    public Object deserializeAsObject(Object rawJsonElement, MetaReferences references, Map<String, String> pools) throws InstantiationException, IllegalAccessException {
        if(rawJsonElement == null)return null;
        String name = ((JsonPrimitive)rawJsonElement).getAsString();
        if(references.getOldObjects().containsKey(name)){
            if(references.getNewObjects().containsKey(name))return references.getNewObjects().get(name);
            Object newInstance = super.deserializeAsObject(SerializeUtil.gson.fromJson(pools.get(name),JsonElement.class),references,pools);
            sync(references.getOldObjects().get(name),newInstance,rawJsonElement,references,pools);
            references.getNewObjects().put(name,newInstance);
            references.getNewNames().put(newInstance,name);
            return references.getOldObjects().get(name);
        }
        else {
            Object newInstance = super.deserializeAsObject(SerializeUtil.gson.fromJson(pools.get(name),JsonElement.class),references,pools);
            references.getOldObjects().put(name,newInstance);
            references.getOldNames().put(newInstance,name);
            return newInstance;
        }
    }


    @Override
    public void sync(Object oldInstance, Object newInstance, Object rawInstance, MetaReferences references, Map<String, String> pools) throws IllegalAccessException, InstantiationException {
        if(oldInstance == null || newInstance == null)return;
        JsonElement jsonElement = (JsonElement) rawInstance;
        if(jsonElement.isJsonArray() && generics != null && oldInstance instanceof Collection && newInstance instanceof Collection){
            ArrayList<Object> items = new ArrayList<>();
            for (JsonElement item : jsonElement.getAsJsonArray()){
                Object newItem = deserializeAsObject(item,references,pools);
                Object oldItem = references.getOldObjects().get(item.getAsString());
                if(oldItem == null) items.add(newItem);
                else items.add(oldItem);
            }
        }
        else if(generics != null && oldInstance instanceof Map && newInstance instanceof Map){
            ((Map<?, ?>) oldInstance).clear();
            ((Map<?, ?>) oldInstance).putAll((Map) newInstance);
        }
        else if(fields.size() != 0){
            for (MetaField metaField : fields.values()){
                Field field = metaField.getField();
                Object newValue = field.get(newInstance);
                field.set(oldInstance,newValue);
            }
        }
    }
}
