package com.qins.net.component;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.meta.annotation.Async;
import com.qins.net.meta.annotation.Sync;
import com.qins.net.meta.core.MetaField;
import com.qins.net.meta.core.MetaParameter;
import com.qins.net.util.SerializeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class StandardMetaParameter extends MetaParameter {
    public Gson gson;
    public StandardMetaParameter(Parameter parameter) {
        super(parameter);
        if(sync){
            gson = new Gson().newBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    return fieldAttributes.getAnnotation(Sync.class) == null;
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            }).create();
        }
        else {
            gson = new Gson().newBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    return fieldAttributes.getAnnotation(Async.class) != null;
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            }).create();
        }
    }

    @Override
    public void onException(Exception exception) {
        Console.error(exception.getMessage());
    }

    @Override
    public void onLog(TrackLog log) {
        Console.log(log.getMessage());
    }

    @Override
    public String serialize(Object instance) {
        return gson.toJson(instance,instanceClass);
    }

    @Override
    public Object deserialize(String instance) {
        return gson.fromJson(instance,instanceClass);
    }

    @Override
    public void sync(Object oldInstance, Object newInstance) {
        if(newInstance == null)return;
        for (MetaField metaField:fields.values()){
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
