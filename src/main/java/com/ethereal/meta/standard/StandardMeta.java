package com.ethereal.meta.standard;

import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.service.core.Service;
import com.ethereal.meta.util.SerializeUtil;
import com.google.gson.*;
import com.google.gson.annotations.Expose;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class StandardMeta extends Meta {
    protected ArrayList<Field> fields = new ArrayList<>();
    @Override
    protected void onConfigure() {

    }

    @Override
    protected void onRegister() {

    }

    @Override
    protected void onInstance() {

    }


    @Override
    protected void onInitialize() {
        for (Field field:instanceClass.getDeclaredFields()){
            if(field.getAnnotation(Expose.class) != null){
                field.setAccessible(true);
                fields.add(field);
            }
        }
        SerializeUtil.gson = SerializeUtil.gson.newBuilder().registerTypeAdapter(instanceClass.getSuperclass(), new JsonDeserializer() {
            @Override
            public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                return jsonDeserializationContext.deserialize(jsonElement,instanceClass);
            }
        }).create();
    }

    @Override
    protected void onUninitialize() {
        
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
        return SerializeUtil.gson.toJson(instance,instanceClass);
    }

    @Override
    public Object deserialize(String instance) {
        if(instance == null) {
            try {
                return proxyClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                onException(e);
            }
        }
        return SerializeUtil.gson.fromJson(instance,proxyClass);
    }

    @Override
    public void sync(Object oldInstance, Object newInstance) {
        for (Field field:fields){
            try {
                field.set(oldInstance,field.get(newInstance));
            } catch (IllegalAccessException e) {
                onException(e);
            }
        }
    }
}
