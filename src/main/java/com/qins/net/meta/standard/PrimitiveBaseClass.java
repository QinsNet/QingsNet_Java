package com.qins.net.meta.standard;

import com.google.gson.JsonPrimitive;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.request.core.RequestReferences;
import com.qins.net.service.core.ServiceReferences;
import com.qins.net.util.SerializeUtil;

import java.lang.reflect.InvocationTargetException;

public class PrimitiveBaseClass extends BaseClass {

    public PrimitiveBaseClass(String name, Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(name, instanceClass);
    }

    @Override
    public String serialize(Object instance, RequestReferences references) throws SerializeException {
        return this.getName()  + "@" +  instance;
    }

    @Override
    public Object deserialize(String rawInstance, RequestReferences references) throws DeserializeException {
        return SerializeUtil.gson.fromJson(rawInstance.split("@")[1],instanceClass);
    }

    @Override
    public String serialize(Object instance, ServiceReferences references) throws SerializeException {
        return this.getName()  + "@" +  instance;
    }

    @Override
    public Object deserialize(String rawInstance, ServiceReferences references) throws DeserializeException {
        return SerializeUtil.gson.fromJson(rawInstance.split("@")[1],instanceClass);
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
    public <T> T newInstance() throws NewInstanceException {
        throw new NewInstanceException("基本数据类型无法无参实例化");
    }

}
