package com.qins.net.meta.core;

import com.qins.net.meta.annotation.Components;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    }
    public abstract Object deserialize(Object jsonElement, MetaReferences references, Map<String,Object> pools) throws InstantiationException, IllegalAccessException;

    public void onException(TrackException.ExceptionCode code, String message) {
        onException(new TrackException(code,message));
    }

    protected void link() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Field field : AnnotationUtil.getFields(instanceClass, Meta.class)){
            field.setAccessible(true);
            MetaField metaField = components.metaField().getConstructor(Field.class,Components.class).newInstance(field,components);
            fields.put(metaField.name, metaField);
        }
    }

    public abstract Object serialize(Object instance, MetaReferences references, Map<String,Object> pools) throws IllegalAccessException;

    public abstract void onException(Exception exception);

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message,this));
    }

    public abstract void onLog(TrackLog log);
}
