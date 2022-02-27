package com.qins.net.meta.standard;

import com.google.gson.*;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaField;
import com.qins.net.util.SerializeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

public abstract class StandardMetaClass extends MetaClass {
    public StandardMetaClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(instanceClass);
    }
    @Override
    public Object serializeAsObject(Object instance) throws IllegalAccessException {
        if(instance == null)return null;
        else if(instance instanceof Iterable){
            JsonArray jsonArray = new JsonArray();
            for (Object item : ((Iterable)instance)){
                JsonElement msg = (JsonElement) generics[0].serializeAsObject(item);
                jsonArray.add(msg);
            }
            return jsonArray;
        }
        else if(instance instanceof Map){
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<Object,Object> item : ((Map<Object,Object>) instance).entrySet()){
                jsonObject.add(generics[0].serialize(item.getKey()), (JsonElement) generics[1].serializeAsObject(instance));
            }
            return jsonObject;
        }
        else if(fields.size() != 0){
            JsonObject jsonObject = new JsonObject();
            for (MetaField metaField : fields.values()){
                Object object = metaField.getField().get(instance);
                if(object == null)continue;
                jsonObject.add(metaField.getName(), (JsonElement) metaField.getBaseClass().serializeAsObject(object));
            }
            return jsonObject;
        }
        else return SerializeUtil.gson.toJsonTree(instance,instanceClass);
    }

    @Override
    public Object deserializeAsObject(Object rawJsonElement) throws InstantiationException, IllegalAccessException {
        JsonElement jsonElement = (JsonElement) rawJsonElement;
        if(rawJsonElement == null)return null;
        else if(jsonElement.isJsonNull()) return null;
        else if(jsonElement.isJsonArray()){
            Collection instance = (Collection) proxyClass.newInstance();
            for (JsonElement element : jsonElement.getAsJsonArray()){
                instance.add(generics[0].deserializeAsObject(element));
            }
            return instance;
        }
        else if(jsonElement.isJsonObject() && Map.class.isAssignableFrom(instanceClass)){
            Map map = (Map) proxyClass.newInstance();
            for (Map.Entry<String,JsonElement> item : jsonElement.getAsJsonObject().entrySet()){
                map.put(generics[0].deserialize(item.getKey()),generics[1].deserializeAsObject(item.getValue()));
            }
            return map;
        }
        else if(jsonElement.isJsonObject()){
            Object baseInstance = proxyClass.newInstance();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (MetaField metaField : fields.values()){
                JsonElement value = jsonObject.get(metaField.getName());
                if(value == null)continue;
                metaField.getField().set(baseInstance,metaField.getBaseClass().deserializeAsObject(value));
            }
            return baseInstance;
        }
        else return SerializeUtil.gson.fromJson(jsonElement,proxyClass);
    }

    @Override
    public String serialize(Object instance) throws IllegalAccessException {
        return SerializeUtil.gson.toJson(serializeAsObject(instance));
    }

    @Override
    public Object deserialize(String rawInstance) throws InstantiationException, IllegalAccessException {
        return deserializeAsObject(SerializeUtil.gson.fromJson(rawInstance,JsonElement.class));
    }

    @Override
    public <T> T newInstance(String rawInstance) throws NewInstanceException, InstantiationException, IllegalAccessException {
        return (T) deserialize(rawInstance);
    }

    @Override
    public void onException(Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void onLog(TrackLog log) {
        Console.log(log.getMessage());
    }

}
