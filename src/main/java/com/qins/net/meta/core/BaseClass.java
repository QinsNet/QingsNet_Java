package com.qins.net.meta.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qins.net.meta.annotation.Components;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class BaseClass {
    protected Class<?> instanceClass;
    protected BaseClass[] generics;
    protected HashMap<String, MetaField> fields = new HashMap<>();
    protected Components components;

    public BaseClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.instanceClass = instanceClass;
        components = instanceClass.getAnnotation(Components.class);
        if(components == null)components = Components.class.getAnnotation(Components.class);
        for (Field field : AnnotationUtil.getFields(instanceClass, Meta.class)){
            field.setAccessible(true);
            MetaField metaField = components.metaField().getConstructor(Field.class,Components.class).newInstance(field,components);
            fields.put(metaField.name, metaField);
        }
    }

    public abstract Object serializeAsObject(Object instance) throws IllegalAccessException;
    public abstract String serialize(Object instance) throws IllegalAccessException;
    public abstract Object deserializeAsObject(Object rawInstance) throws InstantiationException, IllegalAccessException;
    public abstract Object deserialize(String rawInstance) throws InstantiationException, IllegalAccessException;

    public void sync(Object oldInstance,Object newInstance) throws IllegalAccessException {
        if(generics != null && oldInstance instanceof Collection && newInstance instanceof Collection){
            ((Collection<?>) oldInstance).clear();
            ((Collection<?>) oldInstance).addAll((Collection) newInstance);
        }
        else if(generics != null && oldInstance instanceof Map && newInstance instanceof Map){
            ((Map<?, ?>) oldInstance).clear();
            ((Map<?, ?>) oldInstance).putAll((Map) newInstance);
        }
        else if(fields.size() != 0){
            for (MetaField metaField : fields.values()){
                Field field = metaField.getField();
                Object newValue = field.get(newInstance);
                field.set(oldInstance,newValue);
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
}
