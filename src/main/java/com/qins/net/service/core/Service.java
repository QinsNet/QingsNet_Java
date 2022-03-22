package com.qins.net.service.core;

import com.qins.net.core.aop.annotation.AfterEvent;
import com.qins.net.core.aop.annotation.BeforeEvent;
import com.qins.net.core.aop.annotation.ExceptionEvent;
import com.qins.net.core.aop.context.AfterEventContext;
import com.qins.net.core.aop.context.BeforeEventContext;
import com.qins.net.core.aop.context.EventContext;
import com.qins.net.core.aop.context.ExceptionEventContext;
import com.qins.net.core.entity.*;
import com.qins.net.core.exception.*;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.field.Sync;
import com.qins.net.meta.core.*;
import com.qins.net.meta.standard.StandardMetaSerialize;
import com.qins.net.request.core.RequestContext;
import com.qins.net.service.event.InterceptorEvent;
import com.qins.net.service.event.delegate.InterceptorDelegate;
import com.qins.net.util.AnnotationUtil;
import com.sun.xml.internal.ws.client.ResponseContext;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;


public abstract class Service implements IService {
    @Getter
    protected final ServiceConfig config = new ServiceConfig();
    @Getter
    protected final HashMap<String,MetaMethod> methods = new HashMap<>();
    @Getter
    protected final MetaClass metaClass;
    @Getter
    protected final InterceptorEvent interceptorEvent = new InterceptorEvent();

    public Service(MetaClass metaClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, LoadClassException, TrackException {
        this.metaClass = metaClass;
        for (Method method: AnnotationUtil.getMetaMethods(metaClass.getInstanceClass())){
            if((method.getModifiers() & Modifier.ABSTRACT) != 0)continue;
            MetaMethod metaMethod = metaClass.getComponents().metaMethod().getConstructor(Method.class, Components.class).newInstance(method,metaClass.getComponents());
            methods.put(metaMethod.getName(), metaMethod);
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
    public void receive(ServiceContext context) {
        try {
            context.setMetaMethod(methods.get(context.getMapping()));
            if(context.getMetaMethod() == null){
                throw new ResponseException(ResponseException.ExceptionCode.NotFoundMethod, String.format("Mapping:%s 未找到",context.getRequestMeta().getMapping()));
            }
            if(onInterceptor(context.getRequestMeta())){
                context.setInstance(metaClass.newInstance());
                MetaMethod metaMethod = context.getMetaMethod();
                //处理请求体
                handleRequestMeta(context);
                //Before
                beforeEvent(metaMethod.getMethod(),context);
                //Invoke
                try{
                    Object[] args = new Object[metaMethod.getParameters().size()];
                    int i=0;
                    for (String name : metaMethod.getParameters().keySet()){
                        args[i++] = context.getParams().get(name);
                    }
                    context.setResult(metaMethod.getMethod().invoke(context.getInstance(),args));
                }
                catch (Exception e){
                    exceptionEvent(metaMethod.getMethod(),context,e);
                }
                //After
                afterEvent(metaMethod.getMethod(),context,context.getResult());

                context.setResponseMeta(buildResponseMeta(context));
            }
            else throw new ResponseException(ResponseException.ExceptionCode.Intercepted,"请求已被拦截",null);
        }
        catch (Exception e){
            context.setResponseMeta(new ResponseMeta(e));
        }
    }
    public ResponseMeta buildResponseMeta(ServiceContext context) throws IllegalAccessException, SerializeException {
        ResponseMeta responseMeta = new ResponseMeta();
        context.setResponseMeta(responseMeta);
        //实例
        responseMeta.setInstance(new HashMap<>());
        for (MetaField metaField : metaClass.getSyncFields().values()){
            Object field = metaField.getField().get(context.getInstance());
            responseMeta.getInstance().put(metaField.getName(),StandardMetaSerialize.serialize(field,context.getReferencesContext()));
        }
        //参数
        responseMeta.setParams(new HashMap<>());
        for (MetaParameter parameter : context.getMetaMethod().getSyncParameters().values()){
            Object param = context.getParams().get(parameter.getName());
            responseMeta.getParams().put(parameter.getName(), StandardMetaSerialize.serialize(param,context.getReferencesContext()));
        }
        //返回值
        if(context.getMetaMethod().getMetaReturn().getInstanceClass() != void.class && context.getMetaMethod().getMetaReturn().getInstanceClass() != Void.class){
            responseMeta.setResult(StandardMetaSerialize.serialize(context.getResult(),context.getReferencesContext()));
        }
        //缓冲池
        responseMeta.setReferences(context.getReferencesContext().getSerializePools());
        return responseMeta;
    }
    public void handleRequestMeta(ServiceContext context) throws DeserializeException, NotFoundInstanceFieldException, IllegalAccessException, NotFoundParameterException {
        //引用池
        context.getReferencesContext().setDeserializePools(context.getRequestMeta().getReferences());
        //实例
        for (MetaField metaField : metaClass.getFields().values()){
            Object rawField = context.getRequestMeta().getInstance().get(metaField.getName());
            Object field = StandardMetaSerialize.deserialize(rawField, context.getReferencesContext());
            metaField.getField().set(context.getInstance(),field);
        }
        //参数
        context.setParams(new HashMap<>());
        for (MetaParameter metaParameter : context.getMetaMethod().getParameters().values()){
            Object rawParam = context.getRequestMeta().getParams().get(metaParameter.getName());
            Object param = StandardMetaSerialize.deserialize(rawParam, context.getReferencesContext());
            context.getParams().put(metaParameter.getName(),param);
        }
    }
}
