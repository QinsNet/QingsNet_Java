package com.ethereal.meta.request.core;

import com.ethereal.meta.core.aop.annotation.ExceptionEvent;
import com.ethereal.meta.core.type.AbstractType;
import com.ethereal.meta.core.aop.annotation.AfterEvent;
import com.ethereal.meta.core.aop.annotation.BeforeEvent;
import com.ethereal.meta.core.aop.context.AfterEventContext;
import com.ethereal.meta.core.aop.context.BeforeEventContext;
import com.ethereal.meta.core.aop.context.EventContext;
import com.ethereal.meta.core.aop.context.ExceptionEventContext;
import com.ethereal.meta.core.entity.*;
import com.ethereal.meta.core.type.Param;
import com.ethereal.meta.request.annotation.*;
import com.ethereal.meta.request.aop.annotation.FailEvent;
import com.ethereal.meta.request.aop.annotation.SuccessEvent;
import com.ethereal.meta.request.aop.annotation.TimeoutEvent;
import com.ethereal.meta.request.aop.context.FailEventContext;
import com.ethereal.meta.request.aop.context.SuccessEventContext;
import com.ethereal.meta.request.aop.context.TimeoutEventContext;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Random;

public class RequestInterceptor implements MethodInterceptor {
    private final Random random = new Random();

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Request instance = (Request) o;
        RequestMapping requestMapping = instance.getRequestMapping(method);
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
            AbstractType type = instance.getTypes().get(parameterInfo);
            request.getParams().put(parameterInfo.getName(),type.getSerialize().Serialize(args[idx]));
            params.put(parameterInfo.getName(), args[idx++]);
        }
        BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
        if(beforeEvent != null){
            eventContext = new BeforeEventContext(params,method);
            String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
            instance.getEventManager().invokeEvent(instance.getInstanceManager().get(iocObjectName), beforeEvent.function(), params,eventContext);
        }
        if((requestMapping.getInvoke() & InvokeTypeFlags.Local) == 0) {
            try{
                localResult = methodProxy.invokeSuper(instance,args);
            }
            catch (Exception e){
                ExceptionEvent exceptionEvent = method.getAnnotation(ExceptionEvent.class);
                if(exceptionEvent != null){
                    eventContext = new ExceptionEventContext(params,method,e);
                    String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
                    instance.getEventManager().invokeEvent(instance.getInstanceManager().get(iocObjectName), exceptionEvent.function(),params,eventContext);
                    if(exceptionEvent.isThrow())throw e;
                }
                else throw e;
            }
        }
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            eventContext = new AfterEventContext(params,method,localResult);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            instance.getEventManager().invokeEvent(instance.getInstanceManager().get(iocObjectName), afterEvent.function(), params,eventContext);
        }
        if((requestMapping.getInvoke() & InvokeTypeFlags.Remote) != 0){
            Class<?> return_type = method.getReturnType();
            if(return_type.equals(Void.TYPE)){
                instance.getINetwork().send(request);
            }
            else{
                String  id = String.valueOf(random.nextInt());
                while (instance.getTasks().containsKey(id)){
                    id = String.valueOf(random.nextInt());
                }
                request.setId(id);
                instance.getTasks().put(request.getId(),request);
                try {
                    int timeout = instance.getRequestConfig().getTimeout();
                    if(requestMapping.getTimeout() != -1)timeout = requestMapping.getTimeout();
                    if(instance.getINetwork().send(request)){
                        request.wait(timeout);
                        ResponseMeta respond = request.getResult();
                        if(respond != null){
                            if(respond.getError()!=null){
                                FailEvent failEvent = method.getAnnotation(FailEvent.class);
                                if(failEvent != null){
                                    eventContext = new FailEventContext(params,method,respond.getError());
                                    String iocObjectName = failEvent.function().substring(0, failEvent.function().indexOf("."));
                                    instance.getEventManager().invokeEvent(instance.getInstanceManager().get(iocObjectName), failEvent.function(), params,eventContext);
                                }
                                else throw new TrackException(TrackException.ExceptionCode.Runtime,"来自服务端的报错信息：\n" + respond.getError().getMessage());
                            }
                            Param paramAnnotation = method.getAnnotation(Param.class);
                            AbstractType type = null;
                            if(paramAnnotation != null) type = instance.getTypes().getTypesByName().get(paramAnnotation.type());
                            if(type == null)type = instance.getTypes().getTypesByType().get(return_type);
                            if(type == null)throw new TrackException(TrackException.ExceptionCode.Runtime,String.format("RPC中的%s类型参数尚未被注册！",return_type));
                            remoteResult = type.getDeserialize().Deserialize(respond.getResult());
                            SuccessEvent successEvent = method.getAnnotation(SuccessEvent.class);
                            if(successEvent != null){
                                eventContext = new SuccessEventContext(params,method,respond.getResult());
                                String iocObjectName = successEvent.function().substring(0, successEvent.function().indexOf("."));
                                instance.getEventManager().invokeEvent(instance.getInstanceManager().get(iocObjectName), successEvent.function(),params,eventContext);
                            }
                        }
                        TimeoutEvent timeoutEvent =  method.getAnnotation(TimeoutEvent.class);
                        if(timeoutEvent != null){
                            eventContext = new TimeoutEventContext(params,method);
                            assert beforeEvent != null;
                            String iocObjectName = beforeEvent.function().substring(0, timeoutEvent.function().indexOf("."));
                            instance.getEventManager().invokeEvent(instance.getInstanceManager().get(iocObjectName), timeoutEvent.function(),params,eventContext);
                        }
                    }
                }
                finally {
                    instance.getTasks().remove(id);
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
}
