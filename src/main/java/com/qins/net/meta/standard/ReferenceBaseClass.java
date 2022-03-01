package com.qins.net.meta.standard;

import com.google.gson.*;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.MetaReferences;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ReferenceBaseClass extends StandardBaseClass {

    static Random random = new Random();

    public ReferenceBaseClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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
        Object instance;
        if(references.getDeserializeObjects().containsKey(name)){
            return references.getDeserializeObjects().get(name);
        }
        if(references.getSerializeObjects().containsKey(name)){
            Object oldInstance = references.getSerializeObjects().get(name);
            Object newInstance = super.deserialize(pools.get(name),references,pools);
            sync(oldInstance,newInstance,rawJsonElement,references,pools);
            instance = oldInstance;
        }
        else {
            instance = super.deserialize(pools.get(name),references,pools);
        }
        references.getDeserializeNames().put(instance,name);
        references.getDeserializeObjects().put(name,instance);
        references.getBasesClass().put(name,this);
        return instance;
    }


    @Override
    public void sync(Object oldInstance, Object newInstance, Object rawInstance, MetaReferences references, Map<String, Object> pools) throws IllegalAccessException, InstantiationException {
        if(oldInstance == null || newInstance == null)return;
        JsonElement jsonElement = (JsonElement) rawInstance;
        if(jsonElement.isJsonArray() && generics != null && oldInstance instanceof Collection && newInstance instanceof Collection){
            ArrayList<Object> items = new ArrayList<>();
            for (JsonElement item : jsonElement.getAsJsonArray()){
                Object newItem = this.deserialize(item,references,pools);
                Object oldItem = references.getSerializeObjects().get(item.getAsString());
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
