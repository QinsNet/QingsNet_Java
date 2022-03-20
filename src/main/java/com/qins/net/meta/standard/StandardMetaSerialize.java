package com.qins.net.meta.standard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.meta.core.ReferencesContext;
import javafx.util.Pair;

import java.util.Map;

public class StandardMetaSerialize {
    public static Object serialize(Object instance, ReferencesContext context) throws SerializeException {
        try {
            if(instance == null)return null;
            BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(instance.getClass());
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(baseClass.getName(), (JsonElement)baseClass.serialize(instance,context));
            return jsonObject;
        } catch (LoadClassException | IllegalAccessException e) {
            throw new SerializeException(e);
        }
    }

    public static Object deserialize(Object rawJsonElement, ReferencesContext context) throws DeserializeException {
        try {
            if(rawJsonElement == null)return null;
            JsonObject jsonObject = (JsonObject)rawJsonElement;
            for (Map.Entry<String,JsonElement> item : jsonObject.entrySet()){
                BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(item.getKey());
                return baseClass.deserialize(item.getValue(),context);
            }
            throw new DeserializeException("数据错误");
        } catch (LoadClassException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new DeserializeException(e);
        }
    }

}
