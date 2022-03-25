package com.qins.net.meta.core;

import com.qins.net.core.exception.DeserializeException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.exception.SerializeException;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.entity.TrackLog;
import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.request.core.RequestReferences;
import com.qins.net.service.core.ServiceReferences;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.lang.reflect.InvocationTargetException;


@Getter
@Setter
public abstract class BaseClass{
    protected Class<?> instanceClass;
    protected String name;

    public BaseClass(String name,Class<?> instanceClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.instanceClass = instanceClass;
        this.name = name;

    }

    public abstract String serialize(Object instance, SerializeLang serializeLang, RequestReferences references) throws SerializeException;
    public abstract Object deserialize(String rawInstance, SerializeLang serializeLang, RequestReferences references) throws DeserializeException;
    public abstract String serialize(Object instance, SerializeLang serializeLang, ServiceReferences references) throws SerializeException;
    public abstract Object deserialize(String rawInstance, SerializeLang serializeLang, ServiceReferences references) throws DeserializeException;

    public void onException(TrackException.ExceptionCode code, String message) {
        onException(new TrackException(code,message));
    }


    public abstract void onException(Exception exception);

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message,this));
    }

    public abstract void onLog(TrackLog log);
    public abstract <T> T newInstance() throws NewInstanceException;

}
