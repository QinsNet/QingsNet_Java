package com.qins.net.meta.standard;

import com.google.gson.*;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaField;
import com.qins.net.util.SerializeUtil;
import net.sf.cglib.proxy.Factory;

import javax.rmi.CORBA.Util;
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
    public String serialize(Object instance) throws IllegalAccessException {
        if(instance == null)return null;
        JsonObject jsonObject = new JsonObject();
        for (MetaField metaField : fields.values()){
            Object object = metaField.getField().get(instance);
            if(object == null)continue;
            if(metaField.getElementClass() != null){
                if(object instanceof Iterable){
                    JsonArray jsonArray = new JsonArray();
                    for (Object value : ((Iterable)object)){
                        String msg = metaField.getElementClass().serialize(value);
                        jsonArray.add(msg);
                    }
                    jsonObject.add(metaField.getName(),jsonArray);
                }
                else if (object instanceof Map){
                    JsonObject jsonMap = new JsonObject();
                    for (Map.Entry<Object,Object> value : ((Map<Object,Object>) object).entrySet()){
                        String k;
                        String v;
                        if(metaField.getElementClass().getInstanceClass().isAssignableFrom(value.getKey().getClass())){
                            k = metaField.getElementClass().serialize(value.getKey());
                        }
                        else k = SerializeUtil.gson.toJson(value.getKey());

                        if(metaField.getElementClass().getInstanceClass().isAssignableFrom(value.getValue().getClass())){
                            v = metaField.getElementClass().serialize(value.getValue());
                        }
                        else v = SerializeUtil.gson.toJson(value.getValue());
                        jsonMap.addProperty(k,v);
                    }
                    jsonObject.add(metaField.getName(),jsonMap);
                }
            }
            else {
                String msg = metaField.getBaseClass().serialize(object);
                jsonObject.addProperty(metaField.getName(),msg);
            }
        }
        if(jsonObject.size() == 0)return null;
        return SerializeUtil.gson.toJson(jsonObject);
    }

    @Override
    public Object deserialize(String rawInstance) throws InstantiationException, IllegalAccessException {
        JsonElement jsonElement = SerializeUtil.gson.fromJson(rawInstance,JsonElement.class);
        if(rawInstance == null || jsonElement.isJsonNull())return null;
        else if(jsonElement.isJsonObject()){
            Object baseInstance = proxyClass.newInstance();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (MetaField metaField : fields.values()){
                JsonElement value = jsonObject.get(metaField.getName());
                if(value == null)continue;
                if(metaField.getElementClass() != null){
                    JsonArray jsonArray = value.getAsJsonArray();
                    Collection fieldArray = (Collection) metaField.getField().getType().newInstance();
                    for (JsonElement element : jsonArray){
                        fieldArray.add(metaField.getElementClass().deserialize(element.toString()));
                    }
                    metaField.getField().set(baseInstance,fieldArray);
                }
                else {
                    Object fieldInstance = metaField.getBaseClass().deserialize(value.getAsString());
                    metaField.getField().set(baseInstance,fieldInstance);
                }
            }
            return baseInstance;
        }
        else return SerializeUtil.gson.fromJson(rawInstance,instanceClass);
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
