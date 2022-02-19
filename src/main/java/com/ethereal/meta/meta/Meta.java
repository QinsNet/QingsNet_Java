package com.ethereal.meta.meta;

import com.ethereal.meta.core.aop.EventManager;
import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.core.entity.NodeAddress;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.core.entity.TrackLog;
import com.ethereal.meta.core.instance.InstanceManager;
import com.ethereal.meta.core.type.AbstractTypeManager;
import com.ethereal.meta.meta.annotation.Components;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.request.core.Request;
import com.ethereal.meta.request.core.RequestInterceptor;
import com.ethereal.meta.service.core.Service;
import com.ethereal.meta.util.SerializeUtil;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.ClientInfoStatus;
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
    protected Request request;
    @Getter
    protected Service service;
    @Getter
    protected Class<?> instanceClass;
    @Getter
    protected Class<?> proxyClass;
    @Getter
    protected Field field;

    public abstract String serialize(Object instance);
    public abstract Object deserialize(String instance);
    public abstract void sync(Object oldInstance,Object newInstance);
    protected abstract void onConfigure();
    protected abstract void onRegister();
    protected abstract void onInstance();

    protected void onLink() {
        Class<?> checkClass = instanceClass;
        while (checkClass != null){
            for (Field field : checkClass.getDeclaredFields()){
                MetaMapping metaMapping = field.getAnnotation(MetaMapping.class);
                if(metaMapping != null){
                    Meta meta = newMeta(this,metaMapping.value(), field.getType());
                    if(meta != null){
                        link(meta);
                        field.setAccessible(true);
                        meta.field = field;
                    }
                    else {
                        onException(TrackException.ExceptionCode.NewInstanceError, String.format("%s-%s 生成失败", prefixes, metaMapping.value()));
                    }
                }
            }
            checkClass = checkClass.getSuperclass();
        }
    }

    protected abstract void onInitialize();
    protected abstract void onUninitialize();

    public void onException(TrackException.ExceptionCode code, String message) {
        onException(new TrackException(code,message,this));
    }

    public abstract void onException(Exception exception);

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message,this));
    }

    public abstract void onLog(TrackLog log);

    public <T> T newInstance(NodeAddress local, NodeAddress remote){
        return newInstance(null,local,remote);
    }

    public <T> T newInstance(String raw_instance,NodeAddress local, NodeAddress remote){
        Factory instance = (Factory) deserialize(raw_instance);
        instance.setCallback(0,NoOp.INSTANCE);
        instance.setCallback(1,new RequestInterceptor(request,local,remote));
        for (Meta meta:metas.values()){
            try {
                if(meta.field.get(instance) == null){
                    meta.field.set(instance,meta.newInstance(null,local,remote));
                }
            } catch (IllegalAccessException e) {
                onException(e);
            }
        }
        return (T)instance;
    }

    public void link(Meta child){
        child.parent = this;
        child.prefixes = this.prefixes + "/" + child.mapping;
        metas.put(child.mapping, child);
    }
    public static Meta newMeta(Meta parent, String mapping, Class<?> instanceClass)  {
        Components components = instanceClass.getAnnotation(Components.class);
        if(components == null){
            components = Components.class.getAnnotation(Components.class);
        }
        Meta meta = null;
        try {
            meta = components.meta().getConstructor().newInstance();
            meta.mapping = mapping;
            if(parent != null) parent.link(meta);
            else {
                meta.prefixes = mapping;
            }
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Console.error(e.getMessage());
            return null;
        }
        try {
            meta.instanceClass = instanceClass;
            //Life Cycle
            meta.service = components.service().getConstructor(Meta.class).newInstance(meta);
            meta.request = components.request().getConstructor(Meta.class).newInstance(meta);
            //Proxy Instance
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(instanceClass);
            enhancer.setCallbackTypes(new Class[]{NoOp.class,RequestInterceptor.class});
            Meta finalMeta = meta;
            enhancer.setCallbackFilter(method ->
            {
                if(finalMeta.request.getMethods().containsValue(method)){
                    return 1;
                }
                else return 0;
            });
            meta.proxyClass = enhancer.createClass();
            //Life Cycle
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
