package com.ethereal.meta.request.core;

import com.ethereal.meta.core.type.AbstractType;
import com.ethereal.meta.core.aop.annotation.AfterEvent;
import com.ethereal.meta.core.aop.annotation.BeforeEvent;
import com.ethereal.meta.core.aop.context.AfterEventContext;
import com.ethereal.meta.core.aop.context.BeforeEventContext;
import com.ethereal.meta.core.aop.context.EventContext;
import com.ethereal.meta.core.aop.context.ExceptionEventContext;
import com.ethereal.meta.core.entity.*;
import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.request.annotation.InvokeTypeFlags;
import com.ethereal.meta.request.annotation.RequestMapping;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

public class RequestInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Request instance = (Request) o;
        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
        Object localResult = null;
        EventContext eventContext;
        Parameter[] parameterInfos = method.getParameters();
        com.ethereal.meta.core.entity.RequestMeta request = new com.ethereal.meta.core.entity.RequestMeta();
        request.setMapping(annotation.mapping());
        request.setParams(new HashMap<>(parameterInfos.length -1 ));
        HashMap<String,Object> params = new HashMap<>(parameterInfos.length);
        Net net = null;
        int idx = 0;
        for(Parameter parameterInfo : parameterInfos){
            if(parameterInfo.getAnnotation(com.ethereal.meta.net.annotation.Node.class) != null){
                net = (Net) args[idx];
            }
            else {
                AbstractType type = instance.getTypes().get(parameterInfo);
                request.getParams().put(parameterInfo.getName(),type.getSerialize().Serialize(args[idx]));
            }
            params.put(parameterInfo.getName(), args[idx++]);
        }
        BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
        if(beforeEvent != null){
            eventContext = new BeforeEventContext(params,method);
            String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
            instance.getEventManager().invokeEvent(instance.getInstanceManager().get(iocObjectName), beforeEvent.function(), params,eventContext);
        }
        if((annotation.invokeType() & InvokeTypeFlags.Local) == 0) {
            try{
                localResult = methodProxy.invokeSuper(instance, args);
            }
            catch (Exception e){
                com.ethereal.meta.core.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.meta.core.aop.annotation.ExceptionEvent.class);
                if(exceptionEvent != null){
                    eventContext = new ExceptionEventContext(params,method,e);
                    String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
                    instance.getEventManager().invokeEvent(instance.getInstanceManager().get(iocObjectName), exceptionEvent.function(),params,eventContext);
                    if(exceptionEvent.isThrow())throw e;
                }
                else throw e;
            }
        }
        if((annotation.invokeType() & InvokeTypeFlags.Remote) != 0){
            if(net != null){
                net.getNetwork().send(request);
            }
            else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("{%s}-{%s}首参并非BaseToken实现类！", instance.getClass().getName(),annotation.mapping()));
        }
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            eventContext = new AfterEventContext(params,method,localResult);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            instance.getEventManager().invokeEvent(instance.getInstanceManager().get(iocObjectName), afterEvent.function(), params,eventContext);
        }
        return localResult;
    }
}
