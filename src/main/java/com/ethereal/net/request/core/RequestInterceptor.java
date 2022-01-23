package com.ethereal.net.request.core;

import com.ethereal.net.core.manager.type.AbstractType;
import com.ethereal.net.core.manager.event.Annotation.AfterEvent;
import com.ethereal.net.core.manager.event.Annotation.BeforeEvent;
import com.ethereal.net.core.manager.event.Model.AfterEventContext;
import com.ethereal.net.core.manager.event.Model.BeforeEventContext;
import com.ethereal.net.core.manager.event.Model.EventContext;
import com.ethereal.net.core.manager.event.Model.ExceptionEventContext;
import com.ethereal.net.core.entity.*;
import com.ethereal.net.node.core.Node;
import com.ethereal.net.request.annotation.InvokeTypeFlags;
import com.ethereal.net.request.annotation.RequestMapping;
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
        RequestMeta request = new RequestMeta();
        request.setMapping(annotation.mapping());
        request.setParams(new HashMap<>(parameterInfos.length -1 ));
        HashMap<String,Object> params = new HashMap<>(parameterInfos.length);
        Node node = null;
        int idx = 0;
        for(Parameter parameterInfo : parameterInfos){
            if(parameterInfo.getAnnotation(com.ethereal.net.node.annotation.Node.class) != null){
                node = (Node) args[idx];
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
            instance.getIocManager().invokeEvent(instance.getIocManager().get(iocObjectName), beforeEvent.function(), params,eventContext);
        }
        if((annotation.invokeType() & InvokeTypeFlags.Local) == 0) {
            try{
                localResult = methodProxy.invokeSuper(instance, args);
            }
            catch (Exception e){
                com.ethereal.net.core.manager.event.Annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.net.core.manager.event.Annotation.ExceptionEvent.class);
                if(exceptionEvent != null){
                    eventContext = new ExceptionEventContext(params,method,e);
                    String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
                    instance.getIocManager().invokeEvent(instance.getIocManager().get(iocObjectName), exceptionEvent.function(),params,eventContext);
                    if(exceptionEvent.isThrow())throw e;
                }
                else throw e;
            }
        }
        if((annotation.invokeType() & InvokeTypeFlags.Remote) != 0){
            if(node != null){
                node.getNetwork().send(request);
            }
            else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("{%s}-{%s}首参并非BaseToken实现类！", instance.name,annotation.mapping()));
        }
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            eventContext = new AfterEventContext(params,method,localResult);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            instance.getIocManager().invokeEvent(instance.getIocManager().get(iocObjectName), afterEvent.function(), params,eventContext);
        }
        return localResult;
    }
}
