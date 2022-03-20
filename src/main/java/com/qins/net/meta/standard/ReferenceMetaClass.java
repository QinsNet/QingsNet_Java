package com.qins.net.meta.standard;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.ReferencesContext;
import com.qins.net.util.SerializeUtil;
import javafx.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Random;

public abstract class ReferenceMetaClass extends StandardMetaClass{

    public ReferenceMetaClass(String name,Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(name,instanceClass);
    }
    @Override
    public Object serialize(Object instance, ReferencesContext context) throws SerializeException {
        if(instance == null)return null;
        String name;
        Integer address = System.identityHashCode(instance);
        if(context.getSerializeNames().containsKey(address)){
            return context.getSerializeNames().get(address);
        }
        if(context.getDeserializeNames().containsKey(address)){
            name = context.getDeserializeNames().get(address);
        }
        else name = this.name  + "@" +  Integer.toHexString(instance.hashCode());
        context.getSerializeNames().put(address,name);
        context.getSerializeObjects().put(name,instance);
        context.getSerializePools().put(name,super.serialize(instance,context));
        return new JsonPrimitive(name);
    }
    @Override
    public Object deserialize(Object rawJsonElement, ReferencesContext context) throws DeserializeException {
        try {
            if(rawJsonElement == null)return null;
            String name = ((JsonPrimitive)rawJsonElement).getAsString();
            Object instance;
            if(context.getDeserializeObjects().containsKey(name)){
                return context.getDeserializeObjects().get(name);
            }
            if(context.getSerializeObjects().containsKey(name)){
                instance = context.getSerializeObjects().get(name);
            }
            else {
                instance = newInstance();
            }
            Integer address = System.identityHashCode(instance);
            context.getDeserializeNames().put(address,name);
            context.getDeserializeObjects().put(name,instance);
            super.deserialize(new Pair<>(instance,context.getDeserializePools().get(name)),context);
            return instance;
        } catch (NewInstanceException e) {
            throw new DeserializeException(e);
        }
    }
}
