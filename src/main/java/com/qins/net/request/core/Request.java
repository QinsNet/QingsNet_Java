package com.qins.net.request.core;

import com.qins.net.core.aop.annotation.AfterEvent;
import com.qins.net.core.aop.annotation.BeforeEvent;
import com.qins.net.core.aop.annotation.ExceptionEvent;
import com.qins.net.core.aop.context.AfterEventContext;
import com.qins.net.core.aop.context.BeforeEventContext;
import com.qins.net.core.aop.context.EventContext;
import com.qins.net.core.aop.context.ExceptionEventContext;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.core.entity.TrackException;
import com.qins.net.meta.annotation.MethodMapping;
import com.qins.net.meta.core.MetaMethod;
import com.qins.net.meta.core.MetaParameter;
import com.qins.net.meta.core.MetaNodeField;
import com.qins.net.node.core.Node;
import com.qins.net.request.aop.annotation.FailEvent;
import com.qins.net.request.aop.context.FailEventContext;
import com.qins.net.request.annotation.*;
import com.qins.net.request.aop.annotation.SuccessEvent;
import com.qins.net.request.aop.annotation.TimeoutEvent;
import com.qins.net.request.aop.context.ErrorEventContext;
import com.qins.net.request.aop.context.SuccessEventContext;
import com.qins.net.request.aop.context.TimeoutEventContext;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;



public abstract class Request implements IRequest {
    private final Random random = new Random();
    @Getter
    protected final HashMap<String, MetaMethod> methods = new HashMap<>();
    @Getter
    protected final ConcurrentHashMap<String, RequestMeta> tasks = new ConcurrentHashMap<>();
    @Getter
    protected RequestConfig requestConfig = new RequestConfig();
    @Getter
    protected MetaNodeField metaNodeField;

    public void receive(RequestContext context) {
        try {
            Object oldInstance = context.getInstance();
            Object newInstance = metaNodeField.newInstance(context.getRequestMeta().getInstance());
            metaNodeField.sync(oldInstance,newInstance);
            synchronized (context){
                context.notify();
            }
            for (Map.Entry<String,String> keyValue:context.getResponseMeta().getParams().entrySet()){
                String name = keyValue.getKey();
                String rawParam = keyValue.getValue();
                MetaParameter metaParameter = context.getMetaMethod().getMetaParameters().get(name);
                if(metaParameter == null){
                    metaNodeField.onException(TrackException.ExceptionCode.NotFoundMetaParameter,String.format("%s从远程提供方同步时，%s方法的%s参数未找到", metaNodeField.getMapping(),context.getMetaMethod().getMapping(),name));
                }
                Object oldParam = context.getParams().get(name);
                if(oldParam == null){
                    metaNodeField.onException(TrackException.ExceptionCode.NotFoundParameter,String.format("%s从远程提供方同步时，%s方法的%s参数未找到", metaNodeField.getMapping(),context.getMetaMethod().getMapping(),name));
                }
                Object newParam = metaParameter.deserialize(rawParam);
                metaParameter.sync(oldParam,newParam);
            }
        }
        catch (Exception e){
            metaNodeField.onException(e);
        }
    }

    public Request(MetaNodeField metaNodeField){
        try {
            this.metaNodeField = metaNodeField;
            for (Method method: AnnotationUtil.getMethods(metaNodeField.getInstanceClass(), MethodMapping.class)){
                if(method.isDefault())return;
                MetaMethod metaMethod = new MetaMethod();
                metaMethod.setMethod(method);
                metaMethod.setMethodPact(AnnotationUtil.getMethodPact(method));
                if(metaMethod.getMethodPact() != null && !method.isDefault()){
                    if(method.getReturnType() != void.class){
                        metaMethod.setMetaReturn(metaNodeField.getComponents().metaReturn().getConstructor(Class.class).newInstance(method.getReturnType()));
                    }
                    metaMethod.setMetaParameters(new HashMap<>());
                    for (Parameter parameter : method.getParameters()){
                        MetaParameter metaParameter = metaNodeField.getComponents().metaParameter()
                                .getConstructor(Parameter.class)
                                .newInstance(parameter);
                        metaMethod.getMetaParameters().put(metaMethod.getMethodPact().getMapping(),metaParameter);
                    }
                    methods.put(metaMethod.getMethodPact().getMapping(), metaMethod);
                }
            }
        }
        catch (Exception e){
            metaNodeField.onException(e);
        }
    }
    public RequestMeta prepareRequestMeta(MethodPact methodPact, NodeAddress local, Object instance){
        RequestMeta requestMeta = new RequestMeta();
        requestMeta.setMapping(metaNodeField.getMapping() + "/" + methodPact.getMapping());
        requestMeta.setHost(local.getHost());
        requestMeta.setPort(String.valueOf(local.getPort()));
        requestMeta.setParams(new HashMap<>());
        requestMeta.setInstance(metaNodeField.serialize(instance));
        return requestMeta;
    }
    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy, NodeAddress local,NodeAddress remote) throws Exception {
        RequestContext context = new RequestContext();
        try {
            MethodPact methodPact = AnnotationUtil.getMethodPact(method);
            MetaMethod metaMethod = methods.get(methodPact.getMapping());
            Object result = null;
            context.setInstance(instance);
            context.setMetaMethod(metaMethod);
            context.setParams(new HashMap<>());
            context.setRemote(remote);

            context.setRequestMeta(prepareRequestMeta(methodPact,local,instance));
            int i = 0;
            for (Map.Entry<String,MetaParameter> keyValue : metaMethod.getMetaParameters().entrySet()){
                String name = keyValue.getKey();
                MetaParameter parameter = keyValue.getValue();
                context.getParams().put(name,args[i]);
                context.getRequestMeta().getParams().put(name,parameter.serialize(args[i]));
                i++;
            }
            beforeEvent(method,context);
            Node sender = methodPact.getNodeClass().newInstance();
            sender.setMetaNodeField(metaNodeField);
            sender.setContext(context);
            Class<?> return_type = method.getReturnType();
            int timeout = requestConfig.getTimeout();
            if(methodPact.getTimeout() != -1)timeout = methodPact.getTimeout();
            if(sender.send(context.getRequestMeta(),timeout)){
                synchronized (context){
                    context.wait();
                }
                ResponseMeta responseMeta = context.getResponseMeta();
                if(responseMeta != null){
                    if(responseMeta.getException()!=null){
                        errorEvent(method,context, responseMeta.getException());
                    }
                    if(return_type != void.class && return_type != Void.class){
                        result = metaMethod.getMetaReturn().deserialize(responseMeta.getResult());
                    }
                    successEvent(method,context, responseMeta);
                }
                timeoutEvent(method,context);
            }
            else failEvent(method,context);
            afterEvent(method,context,result);
            return result;
        }
        catch (Exception e){
            metaNodeField.onException(e);
            exceptionEvent(method,context,e);
        }
        catch (Throwable e){
            metaNodeField.onException(new Exception(e));
            exceptionEvent(method,context,new Exception(e));
        }
        return null;
    }
    private void afterEvent(Method method, RequestContext context, Object result) throws TrackException, InvocationTargetException, IllegalAccessException {
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            EventContext eventContext = new AfterEventContext(context.getParams(),method, result);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), afterEvent.function(), context.getParams(),eventContext);
        }
    }
    private void beforeEvent(Method method,RequestContext context) throws TrackException, InvocationTargetException, IllegalAccessException {
        BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
        if(beforeEvent != null){
            EventContext eventContext = new BeforeEventContext(context.getParams(),method);
            String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), beforeEvent.function(), context.getParams(),eventContext);
        }
    }
    private void exceptionEvent(Method method, RequestContext context, Exception e) throws Exception {
        ExceptionEvent exceptionEvent = method.getAnnotation(ExceptionEvent.class);
        if(exceptionEvent != null){
            ExceptionEventContext eventContext = new ExceptionEventContext(context.getParams(),method,e);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), exceptionEvent.function(),context.getParams(),eventContext);
            if(exceptionEvent.isThrow())throw e;
        }
        else throw e;
    }
    private void errorEvent(Method method, RequestContext context, String exception) throws Exception {
        com.qins.net.request.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.qins.net.request.aop.annotation.ExceptionEvent.class);
        if(exceptionEvent != null){
            ErrorEventContext eventContext = new ErrorEventContext(context.getParams(),method, exception);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), exceptionEvent.function(), context.getParams(),eventContext);
            if(exceptionEvent.isThrow()){
                throw new TrackException(TrackException.ExceptionCode.ResponseException,exception);
            }
        }
        else throw new TrackException(TrackException.ExceptionCode.ResponseException,exception);
    }
    private void successEvent(Method method, RequestContext context,ResponseMeta responseMeta) throws Exception {
        SuccessEvent successEvent = method.getAnnotation(SuccessEvent.class);
        if(successEvent != null){
            SuccessEventContext eventContext = new SuccessEventContext(context.getParams(),method,responseMeta.getResult());
            String iocObjectName = successEvent.function().substring(0, successEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), successEvent.function(),context.getParams(),eventContext);
        }
    }
    private void timeoutEvent(Method method, RequestContext context) throws Exception {
        TimeoutEvent timeoutEvent =  method.getAnnotation(TimeoutEvent.class);
        if(timeoutEvent != null){
            TimeoutEventContext eventContext = new TimeoutEventContext(context.getParams(),method);
            String iocObjectName = timeoutEvent.function().substring(0, timeoutEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), timeoutEvent.function(),context.getParams(),eventContext);
        }
    }
    private void failEvent(Method method, RequestContext context) throws Exception {
        FailEvent timeoutEvent =  method.getAnnotation(FailEvent.class);
        if(timeoutEvent != null){
            FailEventContext eventContext = new FailEventContext(context.getParams(),method);
            String iocObjectName = timeoutEvent.function().substring(0, timeoutEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), timeoutEvent.function(),context.getParams(),eventContext);
        }
    }
}
