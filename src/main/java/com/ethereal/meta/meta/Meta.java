package com.ethereal.meta.meta;

import com.ethereal.meta.core.aop.EventManager;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.core.instance.InstanceManager;
import com.ethereal.meta.core.type.AbstractTypeManager;
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
import java.util.HashMap;

public abstract class Meta{
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

    protected Component onComponent(){
        return new Component();
    }
    protected abstract void onConfigure();
    protected abstract void onRegister();
    protected abstract void onInstance();
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

    public static  <T extends Meta> T link(String mapping,Meta parent,Class<T> metaClass) throws IllegalAccessException {
        //Proxy Instance
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(metaClass);
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
        T meta = (T)enhancer.create(new Class[]{Class.class},new Object[]{metaClass});
        meta.mapping = mapping;
        meta.parent = parent;
        meta.prefixes = meta.parent.prefixes + "/" + meta.mapping;
        try {
            //Life Cycle
            Component component = meta.onComponent();
            meta.service = component.getService().getConstructor(Meta.class,Class.class).newInstance(meta, metaClass);
            meta.request = component.getRequest().getConstructor(Meta.class,Class.class).newInstance(meta, metaClass);
            meta.net = component.getNet().getConstructor(Meta.class,Class.class).newInstance(meta, metaClass);

            meta.onConfigure();

            meta.onRegister();

            meta.onInstance();

            meta.onLink(metaClass);

            meta.onInitialize();
            return meta;
        }
        catch (Exception e){
            meta.onException(e);
        }
        return meta;
    }

    public static <T extends Meta> T link(Class<T> metaClass) throws IllegalAccessException {
        return link("",null,metaClass);
    }


    protected void onLink(Class<? extends Meta> metaClass) {
        try {
            for (Field field : metaClass.getFields()){
                MetaMapping metaMapping = field.getAnnotation(MetaMapping.class);
                if(metaMapping != null){
                    Meta meta = link(metaMapping.mapping(),this,(Class<? extends Meta>) field.getType());
                    field.set(this,meta);
                }
            }
        }
        catch (IllegalAccessException exception){
            onException(exception);
        }
    }
}
