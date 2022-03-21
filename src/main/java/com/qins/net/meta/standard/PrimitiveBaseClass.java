package com.qins.net.meta.standard;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.SerializeContext;
import com.qins.net.util.SerializeUtil;

import java.lang.reflect.InvocationTargetException;

public class PrimitiveBaseClass extends StandardBaseClass {

    public PrimitiveBaseClass(String name, Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(name, instanceClass);
    }

    @Override
    public Object serialize(Object instance, SerializeContext context) throws SerializeException {
        if(instance == null)return null;
        return new JsonPrimitive(getName() + "@" + instance);
    }

    @Override
    public Object deserialize(Object rawInstance, SerializeContext context) {
        if(rawInstance == null)return null;
        return SerializeUtil.gson.fromJson(((JsonPrimitive) rawInstance).getAsString().split("@")[1],instanceClass);
    }
}
