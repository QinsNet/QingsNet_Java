package com.ethereal.meta.request.core;

import com.ethereal.meta.core.aop.annotation.AfterEvent;
import com.ethereal.meta.core.aop.annotation.BeforeEvent;
import com.ethereal.meta.core.aop.annotation.ExceptionEvent;
import com.ethereal.meta.core.aop.context.AfterEventContext;
import com.ethereal.meta.core.aop.context.BeforeEventContext;
import com.ethereal.meta.core.aop.context.EventContext;
import com.ethereal.meta.core.aop.context.ExceptionEventContext;
import com.ethereal.meta.core.entity.*;
import com.ethereal.meta.core.type.AbstractType;
import com.ethereal.meta.core.type.Param;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.net.network.INetwork;
import com.ethereal.meta.request.annotation.*;
import com.ethereal.meta.request.aop.annotation.FailEvent;
import com.ethereal.meta.request.aop.annotation.SuccessEvent;
import com.ethereal.meta.request.aop.annotation.TimeoutEvent;
import com.ethereal.meta.request.aop.context.FailEventContext;
import com.ethereal.meta.request.aop.context.SuccessEventContext;
import com.ethereal.meta.request.aop.context.TimeoutEventContext;
import com.ethereal.meta.util.AnnotationUtil;
import lombok.Getter;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;



@RequestAnnotation
public abstract class Request implements IRequest {
    private final Random random = new Random();
    @Getter
    protected final HashMap<String, Method> requests = new HashMap<>();
    @Getter
    protected final HashMap<String, Field> requestFields = new HashMap<>();
    @Getter
    protected final ConcurrentHashMap<String,RequestMeta> tasks = new ConcurrentHashMap<>();
    @Getter
    protected RequestConfig requestConfig;
    @Getter
    protected Meta meta;

    public void receive(ResponseMeta responseMeta) {
        try {
            if(!tasks.containsKey(responseMeta.getId())){
                throw new TrackException(TrackException.ExceptionCode.NotFoundRequest, String.format("请求:%s ID：%s 未找到",meta.getPrefixes(),responseMeta.getId()));
            }
            RequestMeta requestMeta = tasks.remove(responseMeta.getId());
            synchronized (requestMeta){
                requestMeta.setResult(responseMeta);
                requestMeta.notify();
            }
        }
        catch (Exception e){
            meta.onException(e);
        }
    }

    public Request(Meta meta){
        try {
            this.meta = meta;
            for (Method method : meta.getInstanceClass().getMethods()){
                RequestMapping requestMapping = getRequestMapping(method);
                if(requestMapping !=null){
                    if(method.getReturnType() != void.class){
                        Param paramAnnotation = method.getAnnotation(Param.class);
                        if(paramAnnotation != null){
                            if(paramAnnotation.name() != null){
                                String typeName = paramAnnotation.name();
                                if(meta.getTypes().get(typeName) == null){
                                    throw new TrackException(TrackException.ExceptionCode.NotFoundType, String.format("%s-%s-%s抽象类型未找到", meta.getComponent().getInstance().getName() ,method.getName(),paramAnnotation.name()));
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
                                    throw new TrackException(TrackException.ExceptionCode.NotFoundType, String.format("%s-%s-%s抽象类型未找到", meta.getComponent().getInstance().getName() ,method.getName(),paramAnnotation.name()));
                                }
                            }
                        }
                        else if(meta.getTypes().get(parameter.getParameterizedType()) == null){
                            meta.getTypes().add(parameter.getName(),parameter.getType());
                        }
                    }
                    requests.put(requestMapping.getMapping(), method);
                }
            }
            for (Field field:meta.getInstanceClass().getFields()){
                field.getAnnotation()
            }
        }
        catch (Exception e){
            meta.onException(e);
        }
    }
    public Object newRequestInstance(INetwork network){
        //Proxy Instance
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(meta.getInstanceClass());
        RequestInterceptor interceptor = new RequestInterceptor(this,network);
        Callback noOp= NoOp.INSTANCE;
        enhancer.setCallbacks(new Callback[]{noOp,interceptor});
        enhancer.setCallbackFilter(method ->
        {
            if(AnnotationUtil.getAnnotation(method, RequestAnnotation.class) != null){
                return 1;
            }
            else return 0;
        });
        for (Field field : meta.getInstanceClass()){

        }
        Object instance = enhancer.create();
        return instance;
    }
    public RequestMapping getRequestMapping(Method method){
        RequestMapping requestMapping = new RequestMapping();
        if(method.getAnnotation(PostRequest.class) != null){
            PostRequest annotation = method.getAnnotation(PostRequest.class);
            requestMapping.setMapping(annotation.mapping());
            requestMapping.setInvoke(annotation.invoke());
            requestMapping.setTimeout(annotation.timeout());
            requestMapping.setMethod(RequestType.Post);
        }
        else if(method.getAnnotation(GetRequest.class) != null){
            GetRequest annotation = method.getAnnotation(GetRequest.class);
            requestMapping.setMapping(annotation.mapping());
            requestMapping.setInvoke(annotation.invoke());
            requestMapping.setTimeout(annotation.timeout());
            requestMapping.setMethod(RequestType.Get);
        }
        return requestMapping;
    }
    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy, INetwork network){
        try {
            RequestMapping requestMapping = getRequestMapping(method);
            Object localResult = null;
            Object remoteResult = null;
            Object methodResult = null;
            EventContext eventContext;
            Parameter[] parameterInfos = method.getParameters();
            RequestMeta request = new RequestMeta();
            request.setMapping(requestMapping.getMapping());
            request.setParams(new HashMap<>(parameterInfos.length -1 ));
            HashMap<String,Object> params = new HashMap<>(parameterInfos.length);
            int idx = 0;
            for(Parameter parameterInfo : parameterInfos){
                AbstractType type = meta.getTypes().get(parameterInfo);
                request.getParams().put(parameterInfo.getName(),type.serialize(args[idx]));
                params.put(parameterInfo.getName(), args[idx++]);
            }
            BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
            if(beforeEvent != null){
                eventContext = new BeforeEventContext(params,method);
                String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
                meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), beforeEvent.function(), params,eventContext);
            }
            if((requestMapping.getInvoke() & InvokeTypeFlags.Local) == 0) {
                try{
                    localResult = methodProxy.invokeSuper(instance,args);
                }
                catch (Throwable e){
                    ExceptionEvent exceptionEvent = method.getAnnotation(ExceptionEvent.class);
                    if(exceptionEvent != null){
                        eventContext = new ExceptionEventContext(params,method,new Exception(e));
                        String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
                        meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), exceptionEvent.function(),params,eventContext);
                        if(exceptionEvent.isThrow())throw new Exception(e);
                    }
                    else throw new Exception(e);
                }
            }
            AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
            if(afterEvent != null){
                eventContext = new AfterEventContext(params,method,localResult);
                String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
                meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), afterEvent.function(), params,eventContext);
            }
            if((requestMapping.getInvoke() & InvokeTypeFlags.Remote) != 0){
                Class<?> return_type = method.getReturnType();
                if(return_type.equals(Void.TYPE)){
                    network.send(request);
                }
                else{
                    String  id = String.valueOf(random.nextInt());
                    while (tasks.containsKey(id)){
                        id = String.valueOf(random.nextInt());
                    }
                    request.setId(id);
                    tasks.put(request.getId(),request);
                    try {
                        int timeout = requestConfig.getTimeout();
                        if(requestMapping.getTimeout() != -1)timeout = requestMapping.getTimeout();
                        if(network.send(request)){
                            request.wait(timeout);
                            ResponseMeta respond = request.getResult();
                            if(respond != null){
                                if(respond.getError()!=null){
                                    FailEvent failEvent = method.getAnnotation(FailEvent.class);
                                    if(failEvent != null){
                                        eventContext = new FailEventContext(params,method,respond.getError());
                                        String iocObjectName = failEvent.function().substring(0, failEvent.function().indexOf("."));
                                        meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), failEvent.function(), params,eventContext);
                                    }
                                    else throw new TrackException(TrackException.ExceptionCode.Runtime,"来自服务端的报错信息：\n" + respond.getError().getMessage());
                                }
                                Param paramAnnotation = method.getAnnotation(Param.class);
                                AbstractType type = null;
                                if(paramAnnotation != null) type = meta.getTypes().getTypesByName().get(paramAnnotation.name());
                                if(type == null)type = meta.getTypes().getTypesByType().get(return_type);
                                if(type == null)throw new TrackException(TrackException.ExceptionCode.Runtime,String.format("RPC中的%s类型参数尚未被注册！",return_type));
                                remoteResult = type.deserialize(respond.getResult());
                                SuccessEvent successEvent = method.getAnnotation(SuccessEvent.class);
                                if(successEvent != null){
                                    eventContext = new SuccessEventContext(params,method,respond.getResult());
                                    String iocObjectName = successEvent.function().substring(0, successEvent.function().indexOf("."));
                                    meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), successEvent.function(),params,eventContext);
                                }
                            }
                            TimeoutEvent timeoutEvent =  method.getAnnotation(TimeoutEvent.class);
                            if(timeoutEvent != null){
                                eventContext = new TimeoutEventContext(params,method);
                                assert beforeEvent != null;
                                String iocObjectName = beforeEvent.function().substring(0, timeoutEvent.function().indexOf("."));
                                meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), timeoutEvent.function(),params,eventContext);
                            }
                        }
                    }
                    finally {
                        tasks.remove(id);
                    }
                }
            }
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
}
