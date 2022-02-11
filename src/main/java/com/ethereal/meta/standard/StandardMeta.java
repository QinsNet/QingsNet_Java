package com.ethereal.meta.standard;

import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.service.core.Service;

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

}
