package com.ethereal.meta.request.core;

import com.ethereal.meta.core.aop.annotation.AfterEvent;
import com.ethereal.meta.core.aop.annotation.BeforeEvent;
import com.ethereal.meta.core.aop.context.AfterEventContext;
import com.ethereal.meta.core.aop.context.BeforeEventContext;
import com.ethereal.meta.core.aop.context.EventContext;
import com.ethereal.meta.core.aop.context.ExceptionEventContext;
import com.ethereal.meta.core.entity.*;
import com.ethereal.meta.core.entity.Error;
import com.ethereal.meta.core.type.AbstractType;
import com.ethereal.meta.core.type.Param;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.node.core.Node;
import com.ethereal.meta.request.annotation.*;
import com.ethereal.meta.request.aop.annotation.ErrorEvent;
import com.ethereal.meta.request.aop.annotation.FailEvent;
import com.ethereal.meta.request.aop.annotation.SuccessEvent;
import com.ethereal.meta.request.aop.annotation.TimeoutEvent;
import com.ethereal.meta.request.aop.context.ErrorEventContext;
import com.ethereal.meta.request.aop.context.FailEventContext;
import com.ethereal.meta.request.aop.context.SuccessEventContext;
import com.ethereal.meta.request.aop.context.TimeoutEventContext;
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
    protected final ConcurrentHashMap<String,RequestMeta> tasks = new ConcurrentHashMap<>();
    @Getter
    protected RequestConfig requestConfig = new RequestConfig();
    @Getter
    protected Meta meta;

    public void receive(RequestContext context) {
        try {
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

    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy, NodeAddress local,NodeAddress remote){
        try {
            RequestMapping requestMapping = getRequestMapping(method);
            Object localResult = null;
            Object remoteResult = null;
            Object methodResult = null;
            EventContext eventContext;
            Parameter[] parameterInfos = method.getParameters();
            RequestContext context = new RequestContext();
            context.setInstance(instance);
            context.setMethod(method);
            context.setParams(new HashMap<>(parameterInfos.length));
            context.setRemote(remote);

            context.setRequestMeta(new RequestMeta());
            context.getRequestMeta().setMapping(meta.getPrefixes() + "/" + requestMapping.getMapping());
            context.getRequestMeta().setHost(local.getHost());
            context.getRequestMeta().setPort(String.valueOf(local.getPort()));
            context.getRequestMeta().setParams(new HashMap<>());
            for(int i=0;i<parameterInfos.length;i++){
                context.getParams().put(parameterInfos[i].getName(),args[i]);
                AbstractType type = meta.getTypes().get(parameterInfos[i]);
                context.getRequestMeta().getParams().put(parameterInfos[i].getName(),type.serialize(args[i]));
            }
            beforeEvent(method,context);
            if((requestMapping.getInvoke() & InvokeTypeFlags.Local) == 0) {
                try{
                    localResult = methodProxy.invokeSuper(instance,args);
                }
                catch (Throwable e) {
                    exceptionEvent(method,context,new Exception(e));
                }
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
                        if(responseMeta.getError()!=null){
                            errorEvent(method,context, responseMeta.getError());
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
        com.ethereal.meta.core.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.meta.core.aop.annotation.ExceptionEvent.class);
        if(exceptionEvent != null){
            ExceptionEventContext eventContext = new ExceptionEventContext(context.getParams(),method,e);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), exceptionEvent.function(),context.getParams(),eventContext);
            if(exceptionEvent.isThrow())throw e;
        }
        else throw e;
    }
    private void errorEvent(Method method, RequestContext context, Error error) throws Exception {
        ErrorEvent errorEvent = method.getAnnotation(ErrorEvent.class);
        if(errorEvent != null){
            ErrorEventContext eventContext = new ErrorEventContext(context.getParams(),method,error);
            String iocObjectName = errorEvent.function().substring(0, errorEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), errorEvent.function(), context.getParams(),eventContext);
        }
        else throw new TrackException(TrackException.ExceptionCode.Runtime,"来自服务端的报错信息：\n" + error.getMessage());
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
        if(method.getAnnotation(com.ethereal.meta.request.annotation.PostRequest.class) != null){
            RequestMapping requestMapping = new RequestMapping();
            com.ethereal.meta.request.annotation.PostRequest annotation = method.getAnnotation(com.ethereal.meta.request.annotation.PostRequest.class);
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
