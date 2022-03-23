package com.qins.net.meta.standard;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.request.core.RequestReferences;
import com.qins.net.service.core.ServiceReferences;

public class StandardMetaSerialize {

    public static String serialize(Object instance, RequestReferences references) throws SerializeException {
        try {
            if(instance == null)return null;
            BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(instance.getClass());
            return baseClass.serialize(instance,references);
        } catch (LoadClassException e) {
            throw new SerializeException(e);
        }
    }

    public static Object deserialize(String rawInstance,RequestReferences references) throws DeserializeException {
        try {
            if(rawInstance == null)return null;
            BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(rawInstance.split("@")[0]);
            if(baseClass == null) throw new DeserializeException("数据类型错误:" + rawInstance);
            return baseClass.deserialize(rawInstance,references);
        } catch (LoadClassException | ClassNotFoundException e) {
            throw new DeserializeException(e);
        }
    }

    public static String serialize(Object instance, ServiceReferences references) throws SerializeException {
        try {
            if(instance == null)return null;
            BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(instance.getClass());
            return baseClass.serialize(instance,references);
        } catch (LoadClassException e) {
            throw new SerializeException(e);
        }
    }

    public static Object deserialize(String rawInstance,ServiceReferences references) throws DeserializeException {
        try {
            if(rawInstance == null)return null;
            BaseClass baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(rawInstance.split("@")[0]);
            if(baseClass == null) throw new DeserializeException("数据类型错误:" + rawInstance);
            return baseClass.deserialize(rawInstance,references);
        } catch (LoadClassException | ClassNotFoundException e) {
            throw new DeserializeException(e);
        }
    }

}
