package com.qins.net.meta.core;

import com.qins.net.core.entity.TrackException;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.meta.annotation.Async;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.Sync;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class MetaClass{
    @Getter
    protected HashMap<String, MetaField> fields = new HashMap<>();
    @Getter
    protected Components components;
    @Getter
    protected Class<?> instanceClass;
    @Getter
    protected boolean sync = false;
    @Getter
    protected String name;
    public abstract String serialize(Object instance);
    public abstract Object deserialize(String instance);
    public abstract void sync(Object oldInstance,Object newInstance);

    private void onLink() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(sync){
            for (Field field : AnnotationUtil.getFields(instanceClass, Sync.class)){
                field.setAccessible(true);
                MetaField metaField = components.metaField().getConstructor(Field.class).newInstance(field);
                fields.put(metaField.name,metaField);
            }
        }
        else {
            for (Field field : AnnotationUtil.getAllFields(instanceClass)){
                if(field.getAnnotation(Async.class) == null){
                    field.setAccessible(true);
                    MetaField metaField = components.metaField().getConstructor(Field.class).newInstance(field);
                    fields.put(metaField.name,metaField);
                }
            }
        }
    }


    public void onException(TrackException.ExceptionCode code, String message) {
        onException(new TrackException(code,message));
    }

    public abstract void onException(Exception exception);

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message,this));
    }

    public abstract void onLog(TrackLog log);

    public MetaClass(Class<?> instanceClass){
        components = instanceClass.getAnnotation(Components.class);
        if(components == null){
            components = Components.class.getAnnotation(Components.class);
        }
        try {
            this.instanceClass = instanceClass;
            if(instanceClass.getAnnotation(Sync.class) != null)sync = true;
            onLink();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            onException(e);
        }
    }

    public <T> T newInstance(String raw_instance){
        return (T) deserialize(raw_instance);
    }
}
