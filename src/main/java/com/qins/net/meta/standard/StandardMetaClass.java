package com.qins.net.meta.standard;

import com.google.gson.*;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
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
    public void onException(Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void onLog(TrackLog log) {
        Console.log(log.getMessage());
    }

    @Override
    public Object serializeAsObject(Object instance) throws IllegalAccessException {
        if(instance == null)return null;
        JsonObject jsonObject = new JsonObject();
        for (MetaField metaField : fields.values()){
            Object object = metaField.getField().get(instance);
            if(object == null)continue;
            if(metaField.getElementClass() != null){
                if(object instanceof Iterable){
                    JsonArray jsonArray = new JsonArray();
                    for (Object item : ((Iterable)object)){
                        JsonElement msg = (JsonElement) metaField.getElementClass().serializeAsObject(item);
                        jsonArray.add(msg);
                    }
                    jsonObject.add(metaField.getName(),jsonArray);
                }
                else if (object instanceof Map){
                    JsonObject jsonMap = new JsonObject();
                    for (Map.Entry<Object,Object> item : ((Map<Object,Object>) object).entrySet()){
                        JsonElement k;
                        JsonElement v;
                        if(metaField.getElementClass().getInstanceClass().isAssignableFrom(item.getKey().getClass())){
                            k = (JsonElement) metaField.getElementClass().serializeAsObject(item.getKey());
                        }
                        else k = SerializeUtil.gson.toJsonTree(item.getKey());

                        if(metaField.getElementClass().getInstanceClass().isAssignableFrom(item.getValue().getClass())){
                            v = (JsonElement) metaField.getElementClass().serializeAsObject(item.getValue());
                        }
                        else v = SerializeUtil.gson.toJsonTree(item.getValue());
                        jsonMap.add(k.getAsString(), v);
                    }
                    jsonObject.add(metaField.getName(),jsonMap);
                }
            }
            else {
                jsonObject.add(metaField.getName(), (JsonElement) metaField.getBaseClass().serializeAsObject(object));
            }
        }
        if(jsonObject.size() == 0)return null;
        return jsonObject;
    }

    @Override
    public Object deserializeAsObject(Object rawJsonElement) throws InstantiationException, IllegalAccessException {
        if(rawJsonElement == null)return null;
        JsonElement jsonElement = (JsonElement) rawJsonElement;
        if(jsonElement.isJsonObject()){
            Object baseInstance = proxyClass.newInstance();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (MetaField metaField : fields.values()){
                JsonElement value = jsonObject.get(metaField.getName());
                if(value == null)continue;
                if(metaField.getElementClass() != null){
                    JsonArray jsonArray = value.getAsJsonArray();
                    Collection fieldArray = (Collection) metaField.getField().getType().newInstance();
                    for (JsonElement element : jsonArray){
                        fieldArray.add(metaField.getElementClass().deserializeAsObject(element));
                    }
                    metaField.getField().set(baseInstance,fieldArray);
                }
                else {
                    metaField.getField().set(baseInstance,metaField.getBaseClass().deserializeAsObject(value));
                }
            }
            return baseInstance;
        }
        else return SerializeUtil.gson.fromJson(jsonElement,instanceClass);
    }

    @Override
    public void sync(Object oldInstance, Object newInstance) throws IllegalAccessException {
        if(newInstance == null)return;
        for (MetaField metaField : fields.values()){
            Field field = metaField.getField();
            Object newValue = field.get(newInstance);
            field.set(oldInstance,newValue);
        }
    }
}
