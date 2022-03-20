package com.qins.net.meta.standard;

import com.google.gson.JsonElement;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.ReferencesContext;
import com.qins.net.util.SerializeUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class PrimitiveBaseClass extends StandardBaseClass {

    public PrimitiveBaseClass(String name, Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(name, instanceClass);
    }

    @Override
    public Object serialize(Object instance, ReferencesContext context) throws SerializeException {
        if(instance == null)return null;
        return SerializeUtil.gson.toJsonTree(instance,instanceClass);
    }

    @Override
    public Object deserialize(Object rawJsonElement, ReferencesContext context) throws DeserializeException {
        if(rawJsonElement == null)return null;
        return SerializeUtil.gson.fromJson((JsonElement) rawJsonElement,instanceClass);
    }
}
