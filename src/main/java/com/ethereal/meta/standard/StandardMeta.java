package com.ethereal.meta.standard;

import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.service.core.Service;
import com.ethereal.meta.util.SerializeUtil;

import java.lang.reflect.Field;

public class StandardMeta extends Meta {
    @Override
    protected void onConfigure() {

    }

    @Override
    protected void onRegister() {

    }

    @Override
    protected void onInstance() {

    }


    @Override
    protected void onInitialize() {

    }

    @Override
    protected void onUninitialize() {
        
    }

    @Override
    public void onException(Exception exception) {
        Console.error(exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    public void onLog(TrackLog log) {
        Console.log(log.getMessage());
    }

    @Override
    public String serialize(Object instance) {
        return SerializeUtil.gson.toJson(instance,instanceClass);
    }

    @Override
    public Object deserialize(String instance) {
        return SerializeUtil.gson.fromJson(instance,instanceClass);
    }

}
