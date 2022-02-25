package com.qins.net.meta.core;

import com.qins.net.component.Components;
import com.qins.net.core.entity.TrackException;
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
    public BaseClass(Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.instanceClass = instanceClass;
        components = instanceClass.getAnnotation(Components.class);
        if(components == null)components = Components.class.getAnnotation(Components.class);
        onLink();
    }

    public abstract String serialize(Object instance);
    public abstract Object deserialize(String instance);
    public abstract void sync(Object oldInstance,Object newInstance);

    protected void onLink() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Field field : AnnotationUtil.getFields(instanceClass, Meta.class)){
            field.setAccessible(true);
            MetaField metaField = components.metaField().getConstructor(Field.class).newInstance(field);
            fields.put(metaField.name, metaField);
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
