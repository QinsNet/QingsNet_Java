package com.qins.net.meta.standard;

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
        if(instance == null)return null;
        if(instance instanceof String)return (String) instance;
        return SerializeUtil.gson.toJson(instance);
    }

    @Override
    public Object deserialize(String rawInstance) {
        if(rawInstance == null)return null;
        return SerializeUtil.gson.fromJson(rawInstance,instanceClass);
    }

    @Override
    public void sync(Object oldInstance, Object newInstance) throws IllegalAccessException {
        for (MetaField metaField : fields.values()){
            Field field = metaField.getField();
            Object newValue = field.get(newInstance);
            field.set(oldInstance,newValue);
        }
    }
}
