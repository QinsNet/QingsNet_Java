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
import com.qins.net.core.type.AbstractType;
import com.qins.net.core.type.Param;
import com.qins.net.meta.Meta;
import com.qins.net.node.core.Node;
import com.qins.net.request.aop.annotation.FailEvent;
import com.qins.net.request.aop.context.FailEventContext;
import com.qins.net.request.annotation.*;
import com.qins.net.request.aop.annotation.SuccessEvent;
import com.qins.net.request.aop.annotation.TimeoutEvent;
import com.qins.net.request.aop.context.ErrorEventContext;
import com.qins.net.request.aop.context.SuccessEventContext;
import com.qins.net.request.aop.context.TimeoutEventContext;
import lombok.Getter;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;



@RequestAnnotation
public abstract class Request implements IRequest {
    private final Random random = new Random();
    @Getter
    protected final HashMap<String, Method> methods = new HashMap<>();
    @Getter
    protected final ConcurrentHashMap<String, RequestMeta> tasks = new ConcurrentHashMap<>();
    @Getter
    protected RequestConfig requestConfig = new RequestConfig();
    @Getter
    protected Meta meta;

    public void receive(RequestContext context) {
        try {
            Object oldInstance = context.getInstance();
            Object newInstance = meta.newInstance(context.getResponseMeta().getInstance(),new NodeAddress(context.getRequestMeta().getHost(),context.getRequestMeta().getPort()),context.getRemote());
            meta.sync(oldInstance,newInstance);
            synchronized (context){
                context.notify();
            }
        }
        catch (Exception e){
            meta.onException(e);
        }
    }

    public Request(Meta meta){
        try {
            this.meta = meta;
            Class<?> checkClass = meta.getInstanceClass();
            while (checkClass != null){
                for (Method method : checkClass.getMethods()){
                    RequestMapping requestMapping = getRequestMapping(method);
                    if(requestMapping !=null){
                        if(method.getReturnType() != void.class){
                            Param paramAnnotation = method.getAnnotation(Param.class);
                            if(paramAnnotation != null){
                                if(paramAnnotation.name() != null){
                                    String typeName = paramAnnotation.name();
                                    if(meta.getTypes().get(typeName) == null){
                                        throw new TrackException(TrackException.ExceptionCode.NotFoundType, String.format("%s-%s-%s抽象类型未找到", meta.getInstanceClass().getName() ,method.getName(),paramAnnotation.name()));
                                    }
                                }
                            }
                            else if(meta.getTypes().get(method.getReturnType()) == null){
                                meta.getTypes().add(method.getName(),method.getReturnType());
                            }
                        }
                        for (Parameter parameter : method.getParameters()){
                            Param paramAnnotation = method.getAnnotation(Param.class);
                            if(paramAnnotation != null){
                                if(paramAnnotation.name() != null){
                                    String typeName = paramAnnotation.name();
                                    if(meta.getTypes().get(typeName) == null){
                                        throw new TrackException(TrackException.ExceptionCode.NotFoundType, String.format("%s-%s-%s抽象类型未找到",meta.getInstanceClass().getName() ,method.getName(),paramAnnotation.name()));
                                    }
                                }
                            }
                            else if(meta.getTypes().get(parameter.getParameterizedType()) == null){
                                meta.getTypes().add(parameter.getName(),parameter.getType());
                            }
                        }
                        methods.put(requestMapping.getMapping(), method);
                    }
                }
                checkClass = checkClass.getSuperclass();
            }
        }
        catch (Exception e){
            meta.onException(e);
        }
    }
    public void prepareRequestMeta(RequestContext context,RequestMapping requestMapping,NodeAddress local,Object instance){
        context.setRequestMeta(new RequestMeta());
        context.getRequestMeta().setMapping(meta.getPrefixes() + "/" + requestMapping.getMapping());
        context.getRequestMeta().setHost(local.getHost());
        context.getRequestMeta().setPort(String.valueOf(local.getPort()));
        context.getRequestMeta().setParams(new HashMap<>());
        context.getRequestMeta().setInstance(meta.serialize(instance));
    }
    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy, NodeAddress local,NodeAddress remote) throws Exception {
        RequestContext context = new RequestContext();
        try {
            RequestMapping requestMapping = getRequestMapping(method);
            Object localResult = null;
            Object remoteResult = null;
            Object methodResult = null;
            EventContext eventContext;
            Parameter[] parameterInfos = method.getParameters();
            context.setInstance(instance);
            context.setMethod(method);
            context.setParams(new HashMap<>(parameterInfos.length));
            context.setRemote(remote);

            prepareRequestMeta(context,requestMapping,local,instance);
            for(int i=0;i<parameterInfos.length;i++){
                context.getParams().put(parameterInfos[i].getName(),args[i]);
                AbstractType type = meta.getTypes().get(parameterInfos[i]);
                context.getRequestMeta().getParams().put(parameterInfos[i].getName(),type.serialize(args[i]));
            }
            beforeEvent(method,context);
            if((requestMapping.getInvoke() & InvokeTypeFlags.Local) != 0) {
                localResult = methodProxy.invokeSuper(instance,args);
            }
            if((requestMapping.getInvoke() & InvokeTypeFlags.Remote) != 0){
                Node sender = requestMapping.getNodeClass().newInstance();
                sender.setMeta(meta);
                sender.setContext(context);
                Class<?> return_type = method.getReturnType();
                int timeout = requestConfig.getTimeout();
                if(requestMapping.getTimeout() != -1)timeout = requestMapping.getTimeout();
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
                            Param paramAnnotation = method.getAnnotation(Param.class);
                            AbstractType type = null;
                            if(paramAnnotation != null) type = meta.getTypes().getTypesByName().get(paramAnnotation.name());
                            if(type == null)type = meta.getTypes().getTypesByType().get(return_type);
                            if(type == null)throw new TrackException(TrackException.ExceptionCode.Runtime,String.format("RPC中的%s类型参数尚未被注册！",return_type));
                            remoteResult = type.deserialize(responseMeta.getResult());
                        }
                        successEvent(method,context, responseMeta);
                    }
                    timeoutEvent(method,context);
                }
                else failEvent(method,context);
            }
            afterEvent(method,context,localResult);
            if((requestMapping.getInvoke() & InvokeTypeFlags.ReturnRemote) != 0){
                methodResult = remoteResult;
            }
            else if((requestMapping.getInvoke() & InvokeTypeFlags.ReturnLocal) != 0){
                methodResult = localResult;
            }
            return methodResult;
        }
        catch (Exception e){
            meta.onException(e);
            exceptionEvent(method,context,e);
        }
        catch (Throwable e){
            meta.onException(new Exception(e));
            exceptionEvent(method,context,new Exception(e));
        }
        return null;
    }
    private void afterEvent(Method method, RequestContext context, Object localResult) throws TrackException, InvocationTargetException, IllegalAccessException {
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            EventContext eventContext = new AfterEventContext(context.getParams(),method,localResult);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), afterEvent.function(), context.getParams(),eventContext);
        }
    }
    private void beforeEvent(Method method,RequestContext context) throws TrackException, InvocationTargetException, IllegalAccessException {
        BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
        if(beforeEvent != null){
            EventContext eventContext = new BeforeEventContext(context.getParams(),method);
            String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), beforeEvent.function(), context.getParams(),eventContext);
        }
    }
    private void exceptionEvent(Method method, RequestContext context, Exception e) throws Exception {
        ExceptionEvent exceptionEvent = method.getAnnotation(ExceptionEvent.class);
        if(exceptionEvent != null){
            ExceptionEventContext eventContext = new ExceptionEventContext(context.getParams(),method,e);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), exceptionEvent.function(),context.getParams(),eventContext);
            if(exceptionEvent.isThrow())throw e;
        }
        else throw e;
    }
    private void errorEvent(Method method, RequestContext context, String exception) throws Exception {
        com.qins.net.request.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.qins.net.request.aop.annotation.ExceptionEvent.class);
        if(exceptionEvent != null){
            ErrorEventContext eventContext = new ErrorEventContext(context.getParams(),method, exception);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), exceptionEvent.function(), context.getParams(),eventContext);
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
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), successEvent.function(),context.getParams(),eventContext);
        }
    }
    private void timeoutEvent(Method method, RequestContext context) throws Exception {
        TimeoutEvent timeoutEvent =  method.getAnnotation(TimeoutEvent.class);
        if(timeoutEvent != null){
            TimeoutEventContext eventContext = new TimeoutEventContext(context.getParams(),method);
            String iocObjectName = timeoutEvent.function().substring(0, timeoutEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), timeoutEvent.function(),context.getParams(),eventContext);
        }
    }
    private void failEvent(Method method, RequestContext context) throws Exception {
        FailEvent timeoutEvent =  method.getAnnotation(FailEvent.class);
        if(timeoutEvent != null){
            FailEventContext eventContext = new FailEventContext(context.getParams(),method);
            String iocObjectName = timeoutEvent.function().substring(0, timeoutEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), timeoutEvent.function(),context.getParams(),eventContext);
        }
    }
    public RequestMapping getRequestMapping(Method method){
        if(method.getAnnotation(PostRequest.class) != null){
            RequestMapping requestMapping = new RequestMapping();
            PostRequest annotation = method.getAnnotation(PostRequest.class);
            requestMapping.setMapping(annotation.value());
            requestMapping.setInvoke(annotation.invoke());
            requestMapping.setTimeout(annotation.timeout());
            requestMapping.setNodeClass(annotation.node());
            requestMapping.setMethod(RequestType.Post);
            return requestMapping;
        }
        else if(method.getAnnotation(GetRequest.class) != null){
            RequestMapping requestMapping = new RequestMapping();
            GetRequest annotation = method.getAnnotation(GetRequest.class);
            requestMapping.setMapping(annotation.value());
            requestMapping.setInvoke(annotation.invoke());
            requestMapping.setTimeout(annotation.timeout());
            requestMapping.setNodeClass(annotation.node());
            requestMapping.setMethod(RequestType.Command);
            return requestMapping;
        }
        else if(method.getAnnotation(MetaRequest.class) != null){
            RequestMapping requestMapping = new RequestMapping();
            MetaRequest annotation = method.getAnnotation(MetaRequest.class);
            requestMapping.setMapping(annotation.value());
            requestMapping.setInvoke(annotation.invoke());
            requestMapping.setTimeout(annotation.timeout());
            requestMapping.setNodeClass(annotation.node());
            requestMapping.setMethod(RequestType.Command);
            return requestMapping;
        }
        return null;
    }
}
