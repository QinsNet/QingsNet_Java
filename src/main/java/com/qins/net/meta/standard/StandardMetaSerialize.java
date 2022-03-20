package com.qins.net.meta.standard;

import com.google.gson.JsonPrimitive;
import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.ReferencesContext;
import javafx.util.Pair;

public class StandardMetaSerialize {
    public static Object serialize(Object instance, ReferencesContext context) throws SerializeException {
        try {
            if(instance == null)return null;
            String name;
            if(context.getSerializeNames().containsKey(instance)){
                return context.getSerializeNames().get(instance);
            }
            if(context.getDeserializeNames().containsKey(instance)){
                name = context.getDeserializeNames().get(instance);
            }
            else {
                Class<?> instanceClass = instance.getClass();
                name = instance + "&" + instanceClass.getName();
                context.getSerializeNames().put(instance,name);
                context.getSerializeObjects().put(name,instance);
                BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(instanceClass);
                context.getSerializePools().put(name,baseClass.serialize(instance,context));
            }
            return new JsonPrimitive(name);
        }
        catch (Exception e){
            throw new SerializeException(e);
        }
    }

    public static Object deserialize(Object rawJsonElement, ReferencesContext context) throws DeserializeException {
        try {
            if(rawJsonElement == null)return null;
            String instanceName = ((JsonPrimitive)rawJsonElement).getAsString();
            Object instance;
            if(context.getDeserializeObjects().containsKey(instanceName)){
                return context.getDeserializeObjects().get(instanceName);
            }

            String[] info = instanceName.split("&");
            String name = info[0];
            String type = info[1];
            BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(type);
            if(context.getSerializeObjects().containsKey(instanceName)){
                instance = context.getSerializeObjects().get(instanceName);
            }
            else {
                instance = baseClass.newInstance();
            }
            context.getDeserializeNames().put(instance,name);
            context.getDeserializeObjects().put(name,instance);
            baseClass.deserialize(new Pair<>(instance,name),context);
            return instance;
        }
        catch (Throwable e){
            throw new DeserializeException(e);
        }
    }

}
