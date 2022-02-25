package com.qins.net.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.util.SerializeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class StandardBaseClass extends BaseClass {

    public StandardBaseClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(instanceClass);
    }

    @Override
    public void onException(Exception exception) {
        Console.error(exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    public void onLog(TrackLog log) {
        Console.log(log.getMessage());
    }

    @Override
    public String serialize(Object instance) {
        if(instance == null)return null;
        try {
            JsonObject jsonObject = new JsonObject();
            for (MetaField metaField : fields.values()){
                Object object = metaField.getField().get(instance);
                if(object == null)continue;
                jsonObject.add(metaField.getName(), new JsonPrimitive(metaField.getBaseClass().serialize(object)));
            }
            return SerializeUtil.gson.toJson(jsonObject);
        } catch (IllegalAccessException e) {
            onException(e);
            return null;
        }
    }

    @Override
    public Object deserialize(String instance) {
        if(instance == null)return null;
        try {
            JsonObject jsonObject = SerializeUtil.gson.fromJson(instance,JsonObject.class);
            Object proxyInstance = instanceClass.newInstance();
            for (MetaField metaField : fields.values()){
                JsonElement value = jsonObject.get(metaField.getName());
                if(value == null)continue;
                Object fieldInstance = metaField.getBaseClass().deserialize(value.getAsString());
                metaField.getField().set(proxyInstance,fieldInstance);
            }
            return proxyInstance;
        }
        catch (Exception e){
            onException(e);
            return null;
        }
    }

    @Override
    public void sync(Object oldInstance, Object newInstance) {
        if(newInstance == null)return;
        for (MetaField metaField: fields.values()){
            try {
                Field field = metaField.getField();
                Object value = field.get(newInstance);
                if(value != null){
                    field.set(oldInstance,value);
                }
            } catch (IllegalAccessException e) {
                onException(e);
            }
        }
    }
}
