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
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaMethod;
import com.qins.net.meta.core.MetaParameter;
import com.qins.net.node.core.Node;
import com.qins.net.request.aop.annotation.FailEvent;
import com.qins.net.request.aop.context.FailEventContext;
import com.qins.net.request.aop.annotation.SuccessEvent;
import com.qins.net.request.aop.annotation.TimeoutEvent;
import com.qins.net.request.aop.context.ErrorEventContext;
import com.qins.net.request.aop.context.SuccessEventContext;
import com.qins.net.request.aop.context.TimeoutEventContext;
import lombok.Getter;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public abstract class Request implements IRequest {
    @Getter
    protected final HashMap<String, MetaMethod> methods = new HashMap<>();
    @Getter
    protected final ConcurrentHashMap<String, RequestMeta> tasks = new ConcurrentHashMap<>();
    @Getter
    protected RequestConfig requestConfig = new RequestConfig();
    @Getter
    protected MetaClass metaClass;

    public void receive(RequestContext context) {
        try {
            Object oldInstance = context.getInstance();
            Object newInstance = metaClass.newInstance(context.getRequestMeta().getInstance(),context.getLocal(),context.getRemote());
            metaClass.sync(oldInstance,newInstance);
            synchronized (context){
                context.notify();
            }
            if(context.getResponseMeta().getParams() != null){
                for (Map.Entry<String,String> keyValue:context.getResponseMeta().getParams().entrySet()){
                    String name = keyValue.getKey();
                    String rawParam = keyValue.getValue();
                    MetaParameter metaParameter = context.getMetaMethod().getMetaParameters().get(name);
                    if(metaParameter == null){
                        throw new TrackException(TrackException.ExceptionCode.NotFoundMetaParameter,String.format("%s从远程提供方同步时，%s方法的%s参数未找到", metaClass.getName(),context.getMetaMethod().getName(),name));
                    }
                    Object oldParam = context.getParams().get(name);
                    if(oldParam == null){
                        throw new TrackException(TrackException.ExceptionCode.NotFoundParameter,String.format("%s从远程提供方同步时，%s方法的%s参数未找到", metaClass.getName(),context.getMetaMethod().getName(),name));
                    }
                    Object newParam = metaParameter.getBaseClass().deserialize(rawParam);
                    metaParameter.getBaseClass().sync(oldParam,newParam);
                }
            }
        }
        catch (Exception e){
            metaClass.onException(e);
        }
    }

    public Request(MetaClass metaClass){
        try {
            this.metaClass = metaClass;
            Class<?> checkClass = metaClass.getInstanceClass();
            while (checkClass != null){
                for (Method method: checkClass.getDeclaredMethods()){
                    if(method.getAnnotation(Meta.class) == null)continue;
                    if((method.getModifiers() & Modifier.ABSTRACT) == 0)continue;
                    MetaMethod metaMethod = new MetaMethod(method);
                    methods.put(metaMethod.getName(), metaMethod);
                }
                checkClass = checkClass.getSuperclass();
            }
        }
        catch (Exception e){
            metaClass.onException(e);
        }
    }
    public RequestMeta prepareRequestMeta(MetaMethod metaMethod, NodeAddress local, Object instance){
        RequestMeta requestMeta = new RequestMeta();
        requestMeta.setMapping(metaClass.getName() + "/" + metaMethod.getName());
        requestMeta.setHost(local.getHost());
        requestMeta.setPort(String.valueOf(local.getPort()));
        requestMeta.setParams(new HashMap<>());
        requestMeta.setInstance(metaClass.serialize(instance));
        return requestMeta;
    }
    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy, NodeAddress local,NodeAddress remote) throws Exception {
        RequestContext context = new RequestContext();
        try {
            Meta meta = method.getAnnotation(Meta.class);
            MetaMethod metaMethod = methods.get("".equals(meta.value()) ? method.getName() : meta.value());
            Object result = null;
            context.setInstance(instance);
            context.setMetaMethod(metaMethod);
            context.setParams(new HashMap<>());
            context.setRemote(remote);
            context.setLocal(local);
            context.setRequestMeta(prepareRequestMeta(metaMethod,local,instance));

            int i = 0;
            for (Map.Entry<String,MetaParameter> keyValue : metaMethod.getMetaParameters().entrySet()){
                String name = keyValue.getKey();
                MetaParameter parameter = keyValue.getValue();
                context.getParams().put(name,args[i]);
                context.getRequestMeta().getParams().put(name,parameter.getBaseClass().serialize(args[i]));
                i++;
            }
            beforeEvent(method,context);
            Node sender = metaMethod.getMethodPact().getNodeClass().newInstance();
            sender.setMetaClass(metaClass);
            sender.setContext(context);
            Class<?> return_type = method.getReturnType();
            int timeout = requestConfig.getTimeout();
            if(metaMethod.getMethodPact().getTimeout() != -1)timeout = metaMethod.getMethodPact().getTimeout();
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
                        if(MetaClass.class.isAssignableFrom(metaMethod.getMetaReturn().getClass())){
                            ((MetaClass) metaMethod.getMetaReturn()).updateNode(result,local,remote);
                        }
                    }
                    successEvent(method,context, responseMeta);
                }
                timeoutEvent(method,context);
            }
            else failEvent(method,context);
            afterEvent(method,context,result);
            return result;
        }
        catch (Throwable e){
            exceptionEvent(method,context,new Exception(e));
        }
        return null;
    }
    private void afterEvent(Method method, RequestContext context, Object result) throws TrackException, InvocationTargetException, IllegalAccessException {
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            EventContext eventContext = new AfterEventContext(context.getParams(),method, result);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), afterEvent.function(), context.getParams(),eventContext);
        }
    }
    private void beforeEvent(Method method,RequestContext context) throws TrackException, InvocationTargetException, IllegalAccessException {
        BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
        if(beforeEvent != null){
            EventContext eventContext = new BeforeEventContext(context.getParams(),method);
            String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), beforeEvent.function(), context.getParams(),eventContext);
        }
    }
    private void exceptionEvent(Method method, RequestContext context, Exception e) throws Exception {
        ExceptionEvent exceptionEvent = method.getAnnotation(ExceptionEvent.class);
        if(exceptionEvent != null){
            ExceptionEventContext eventContext = new ExceptionEventContext(context.getParams(),method,e);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), exceptionEvent.function(),context.getParams(),eventContext);
            if(exceptionEvent.isThrow())throw e;
        }
        else throw e;
    }
    private void errorEvent(Method method, RequestContext context, String exception) throws Exception {
        com.qins.net.request.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.qins.net.request.aop.annotation.ExceptionEvent.class);
        if(exceptionEvent != null){
            ErrorEventContext eventContext = new ErrorEventContext(context.getParams(),method, exception);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), exceptionEvent.function(), context.getParams(),eventContext);
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
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), successEvent.function(),context.getParams(),eventContext);
        }
    }
    private void timeoutEvent(Method method, RequestContext context) throws Exception {
        TimeoutEvent timeoutEvent =  method.getAnnotation(TimeoutEvent.class);
        if(timeoutEvent != null){
            TimeoutEventContext eventContext = new TimeoutEventContext(context.getParams(),method);
            String iocObjectName = timeoutEvent.function().substring(0, timeoutEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), timeoutEvent.function(),context.getParams(),eventContext);
        }
    }
    private void failEvent(Method method, RequestContext context) throws Exception {
        FailEvent timeoutEvent =  method.getAnnotation(FailEvent.class);
        if(timeoutEvent != null){
            FailEventContext eventContext = new FailEventContext(context.getParams(),method);
            String iocObjectName = timeoutEvent.function().substring(0, timeoutEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), timeoutEvent.function(),context.getParams(),eventContext);
        }
    }
}
