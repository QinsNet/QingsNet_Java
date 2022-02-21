package com.ethereal.meta.standard;

import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.util.SerializeUtil;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class StandardMeta1 extends Meta {
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
    }

    @Override
    public void onLog(TrackLog log) {
        Console.log(log.getMessage());
    }

    @Override
    public String serialize(Object instance) {
        if(collectionClass != null){
            return SerializeUtil.gson.toJson(instance,collectionClass);
        }
        else return SerializeUtil.gson.toJson(instance,instanceClass);
    }

    @Override
    public Object deserialize(String instance) {
        if(instance == null) {
            try {
                if(collectionClass != null){
                    return collectionClass.newInstance();
                }
                else return proxyClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                onException(e);
            }
        }
        if(collectionClass != null){
            return SerializeUtil.gson.fromJson(instance,collectionClass);
        }
        return SerializeUtil.gson.fromJson(instance,proxyClass);
    }

    @Override
    public void sync(Object oldInstance, Object newInstance) {
        if(newInstance == null)return;
        for (Field field:fields){
            try {
                field.set(oldInstance,field.get(newInstance));
            } catch (IllegalAccessException e) {
                onException(e);
            }
        }
    }
}
