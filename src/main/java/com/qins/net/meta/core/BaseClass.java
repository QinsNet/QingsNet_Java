package com.qins.net.meta.core;

import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
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

@Getter
@Setter
public abstract class BaseClass {
    protected Class<?> instanceClass;
    protected HashMap<String, MetaField> fields = new HashMap<>();
    protected Components components;
    protected String name;

    public BaseClass(String name,Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.instanceClass = instanceClass;
        components = instanceClass.getAnnotation(Components.class);
        if(components == null)components = Components.class.getAnnotation(Components.class);
        this.name = name;
    }
    public abstract Object deserialize(Object rawInstance, SerializeContext context) throws InstantiationException, IllegalAccessException, DeserializeException;

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

    public abstract Object serialize(Object instance, SerializeContext context) throws IllegalAccessException, SerializeException;

    public abstract void onException(Exception exception);

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message,this));
    }

    public abstract void onLog(TrackLog log);
    public abstract <T> T newInstance() throws NewInstanceException;

}
