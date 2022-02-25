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
        exception.printStackTrace();
    }

    @Override
    public void onLog(TrackLog log) {
        Console.log(log.getMessage());
    }

    @Override
    public String serialize(Object instance) {
        return SerializeUtil.gson.toJson(instance,instanceClass);
    }

    @Override
    public Object deserialize(String instance) {
        return SerializeUtil.gson.fromJson(instance,instanceClass);
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
