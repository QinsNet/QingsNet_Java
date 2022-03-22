package com.qins.net.meta.standard;

import com.google.gson.JsonPrimitive;
import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.ReferencesContext;

public class StandardMetaSerialize {

    public static Object serialize(Object instance, ReferencesContext context) throws SerializeException {
        try {
            if(instance == null)return null;
            BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(instance.getClass());
            return baseClass.serialize(instance,context);
        } catch (LoadClassException | IllegalAccessException e) {
            throw new SerializeException(e);
        }
    }

    public static Object deserialize(Object rawJsonElement, ReferencesContext context) throws DeserializeException {
        try {
            if(rawJsonElement == null)return null;
            String name = ((JsonPrimitive) rawJsonElement).getAsString();
            BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(name.split("@")[0]);
            if(baseClass == null) throw new DeserializeException("数据类型错误:" + rawJsonElement);
            return baseClass.deserialize(rawJsonElement,context);
        } catch (LoadClassException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new DeserializeException(e);
        }
    }

}
