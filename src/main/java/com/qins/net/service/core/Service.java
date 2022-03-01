package com.qins.net.service.core;

import com.qins.net.core.aop.annotation.AfterEvent;
import com.qins.net.core.aop.annotation.BeforeEvent;
import com.qins.net.core.aop.annotation.ExceptionEvent;
import com.qins.net.core.aop.context.AfterEventContext;
import com.qins.net.core.aop.context.BeforeEventContext;
import com.qins.net.core.aop.context.EventContext;
import com.qins.net.core.aop.context.ExceptionEventContext;
import com.qins.net.core.entity.*;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.TrackException;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.core.*;
import com.qins.net.service.event.InterceptorEvent;
import com.qins.net.service.event.delegate.InterceptorDelegate;
import com.qins.net.core.exception.ResponseException;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;


public abstract class Service implements IService {
    @Getter
    protected ServiceConfig config;
    @Getter
    protected final HashMap<String,MetaMethod> methods = new HashMap<>();
    @Getter
    protected final MetaClass metaClass;
    @Getter
    protected final InterceptorEvent interceptorEvent = new InterceptorEvent();

    public Service(MetaClass metaClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, LoadClassException, TrackException {
        this.metaClass = metaClass;
        Class<?> checkClass = metaClass.getInstanceClass();
        while (checkClass != null){
            for (Method method: checkClass.getDeclaredMethods()){
                if(method.getAnnotation(Meta.class) == null)continue;
                if((method.getModifiers() & Modifier.ABSTRACT) != 0)continue;
                MetaMethod metaMethod = metaClass.getComponents().metaMethod().getConstructor(Method.class, Components.class).newInstance(method,metaClass.getComponents());
                methods.put(metaMethod.getName(), metaMethod);
            }
            checkClass = checkClass.getSuperclass();
        }
    }
    public boolean onInterceptor(RequestMeta requestMeta)
    {
        for (InterceptorDelegate item : interceptorEvent.getListeners())
        {
            if (!item.onInterceptor(requestMeta)) return false;
        }
        return true;
    }

    protected void afterEvent(Method method,ServiceContext context,Object localResult) throws TrackException, InvocationTargetException, IllegalAccessException {
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            EventContext eventContext = new AfterEventContext(context.getParams(),method,localResult);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), afterEvent.function(), context.getParams(),eventContext);
        }
    }
    protected void beforeEvent(Method method,ServiceContext context) throws TrackException, InvocationTargetException, IllegalAccessException {
        BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
        if(beforeEvent != null){
            EventContext eventContext = new BeforeEventContext(context.getParams(),method);
            String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), beforeEvent.function(), context.getParams(),eventContext);
        }
    }
    protected void exceptionEvent(Method method, ServiceContext context, Exception e) throws Exception {
        ExceptionEvent exceptionEvent = method.getAnnotation(ExceptionEvent.class);
        if(exceptionEvent != null){
            ExceptionEventContext eventContext = new ExceptionEventContext(context.getParams(),method,e);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), exceptionEvent.function(),context.getParams(),eventContext);
            if(exceptionEvent.isThrow())throw e;
        }
        else throw e;
    }
    public Object receive(RequestMeta requestMeta) {
        ServiceContext context = new ServiceContext().setReferences(new MetaReferences()).setRequestMeta(requestMeta).setMapping(requestMeta.getMapping().split("/")[2]);
        try {
            MetaMethod metaMethod = methods.get(context.getMapping());
            if(metaMethod == null){
                throw new ResponseException(ResponseException.ExceptionCode.NotFoundMethod, String.format("Mapping:%s 未找到",requestMeta.getMapping()));
            }
            context.setInstance(metaClass.newInstance(context.getRequestMeta().getInstance(),context.getReferences(),requestMeta.getReferences()));
            if(onInterceptor(requestMeta)){
                context.setParams(new HashMap<>());
                for (MetaParameter metaParameter : metaMethod.getParameters().values()){
                    Object rawParam = context.getRequestMeta().getParams().get(metaParameter.getName());
                    Object param = metaParameter.getBaseClass().deserialize(rawParam,context.getReferences(),requestMeta.getReferences());
                    context.getParams().put(metaParameter.getName(),param);
                }
                //Before
                beforeEvent(metaMethod.getMethod(),context);
                //Invoke
                Object localResult = null;
                try{
                    Object[] args = new Object[metaMethod.getParameters().size()];
                    int i=0;
                    for (String name : metaMethod.getParameters().keySet()){
                        args[i++] = context.getParams().get(name);
                    }
                    localResult = metaMethod.getMethod().invoke(context.getInstance(),args);
                }
                catch (Exception e){
                    exceptionEvent(metaMethod.getMethod(),context,e);
                }
                //After
                afterEvent(metaMethod.getMethod(),context,localResult);
                HashMap<String,Object> syncParams = new HashMap<>();
                HashMap<String,Object> references = new HashMap<>();
                for(MetaParameter metaParameter : metaMethod.getMetaParameters().values()){
                    Object param = context.getParams().get(metaParameter.getName());
                    syncParams.put(metaParameter.getName(),metaParameter.getBaseClass().serialize(param,context.getReferences(),references));
                }
                Object instance = metaClass.serialize(context.getInstance(),context.getReferences(),references);
                //Return
                BaseClass metaReturn = metaMethod.getMetaReturn();
                Object returnObject = null;
                if(metaReturn != null){
                    returnObject = metaReturn.serialize(localResult,context.getReferences(),references);
                }

                //补足由于无网络引用造成的未同步
                if(config.isReferencesAllSync()){
                    for (Map.Entry<String,Object> item : context.getReferences().getDeserializeObjects().entrySet()){
                        if(!context.getReferences().getSerializeObjects().containsKey(item.getKey())){
                            Object oldObject = item.getValue();
                            context.getReferences().getBasesClass().get(item.getKey()).serialize(oldObject,context.getReferences(),references);
                        }
                    }
                }
                return new ResponseMeta(instance,syncParams,returnObject,references);
            }
            else throw new ResponseException(ResponseException.ExceptionCode.Intercepted,"请求已被拦截",null);
        }
        catch (Exception e){
            return new ResponseMeta(e);
        }
    }
}
