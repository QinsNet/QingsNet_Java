package com.ethereal.meta.meta;

import com.ethereal.meta.core.aop.EventManager;
import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.core.instance.InstanceManager;
import com.ethereal.meta.core.type.AbstractTypeManager;
import com.ethereal.meta.meta.annotation.Components;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.meta.event.ExceptionEvent;
import com.ethereal.meta.meta.event.LogEvent;
import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.request.annotation.RequestAnnotation;
import com.ethereal.meta.request.core.Request;
import com.ethereal.meta.request.core.RequestInterceptor;
import com.ethereal.meta.service.core.Service;
import com.ethereal.meta.util.AnnotationUtil;
import lombok.Getter;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public abstract class Meta{
    @Getter
    protected static HashMap<String,Meta> root = new HashMap<>();
    @Getter
    protected EventManager eventManager = new EventManager();
    @Getter
    protected AbstractTypeManager types = new AbstractTypeManager();
    @Getter
    protected InstanceManager instanceManager = new InstanceManager();
    @Getter
    protected HashMap<String,Meta> metas = new HashMap<>();
    @Getter
    protected String prefixes;
    @Getter
    protected String mapping;
    @Getter
    protected Meta parent;
    @Getter
    protected final ExceptionEvent exceptionEvent = new ExceptionEvent();
    @Getter
    protected final LogEvent logEvent = new LogEvent();
    @Getter
    protected Request request;
    @Getter
    protected Service service;
    @Getter
    protected Net net;
    @Getter
    protected Object instance;

    protected abstract void onConfigure();
    protected abstract void onRegister();
    protected abstract void onInstance();

    protected void onLink() {
        try {
            for (Field field : instance.getClass().getFields()){
                MetaMapping metaMapping = field.getAnnotation(MetaMapping.class);
                if(metaMapping != null){
                    Meta meta = newInstance(field.getType());
                    if(meta != null){
                        meta.mapping = metaMapping.mapping();
                        meta.parent = this;
                        meta.prefixes = meta.parent.prefixes + "/" + meta.mapping;
                        field.set(this,meta);
                    }
                }
            }
        }
        catch (IllegalAccessException exception){
            onException(exception);
        }
    }

    protected abstract void onInitialize();
    protected abstract void onUninitialize();

    public void onException(TrackException.ExceptionCode code, String message) {
        onException(new TrackException(code,message,this));
    }

    public void onException(Exception exception)  {
        exceptionEvent.onEvent(exception);
    }

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message,this));
    }

    public void onLog(TrackLog log){
        log.setSender(this);
        logEvent.onEvent(log);
    }


    public void update(String msg){

    }

    public String save(){
        return null;
    }

    public static Meta newInstance(Class<?> instanceClass)  {
        Components components = instanceClass.getAnnotation(Components.class);
        if(components == null){
            components = Components.class.getAnnotation(Components.class);
        }
        //Proxy Instance
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(instanceClass);
        RequestInterceptor interceptor = new RequestInterceptor();
        Callback noOp= NoOp.INSTANCE;
        enhancer.setCallbacks(new Callback[]{noOp,interceptor});
        enhancer.setCallbackFilter(method ->
        {
            if(AnnotationUtil.getAnnotation(method, RequestAnnotation.class) != null){
                return 1;
            }
            else return 0;
        });
        Object instance = enhancer.create();
        Meta meta = null;
        try {
            meta = components.meta().getConstructor().newInstance();
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Console.error(e.getMessage());
            return null;
        }
        try {
            meta.instance = instance;
            //Life Cycle
            meta.service = components.service().getConstructor(Meta.class,Class.class).newInstance(meta,instanceClass);
            meta.request = components.request().getConstructor(Meta.class,Class.class).newInstance(meta,instanceClass);
            interceptor.setRequest(meta.request);
            meta.net = components.net().getConstructor(Meta.class,Class.class).newInstance(meta, instanceClass);

            meta.onConfigure();

            meta.onRegister();

            meta.onInstance();

            meta.onLink();

            meta.onInitialize();

        }
        catch (Exception e){
            meta.onException(e);
        }
        return meta;
    }
}
