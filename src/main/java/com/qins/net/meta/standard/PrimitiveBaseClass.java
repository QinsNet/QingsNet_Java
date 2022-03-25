package com.qins.net.meta.standard;

import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.meta.core.BaseClass;
import com.qins.net.request.core.RequestReferences;
import com.qins.net.service.core.ServiceReferences;
import com.qins.net.util.SerializeUtil;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;

@Log4j2
public class PrimitiveBaseClass extends BaseClass {

    public PrimitiveBaseClass(String name, Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(name, instanceClass);
    }

    @Override
    public String serialize(Object instance, SerializeLang serializeLang, RequestReferences references) throws SerializeException {
        return this.getName()  + "@" +  instance;
    }

    @Override
    public Object deserialize(String rawInstance, SerializeLang serializeLang, RequestReferences references) throws DeserializeException {
        return SerializeUtil.gson.fromJson(rawInstance.split("@")[1],instanceClass);
    }

    @Override
    public String serialize(Object instance, SerializeLang serializeLang, ServiceReferences references) throws SerializeException {
        return this.getName()  + "@" +  instance;
    }

    @Override
    public Object deserialize(String rawInstance, SerializeLang serializeLang, ServiceReferences references) throws DeserializeException {
        return SerializeUtil.gson.fromJson(rawInstance.split("@")[1],instanceClass);
    }

    @Override
    public void onException(Exception exception) {
        log.error(exception);
    }

    @Override
    public void onLog(TrackLog trackLog) {
        log.info(trackLog.toString());
    }

    @Override
    public <T> T newInstance() throws NewInstanceException {
        throw new NewInstanceException("基本数据类型无法无参实例化");
    }

}
