package com.ethereal.server.Request.Abstract;

import com.ethereal.server.Core.Manager.AbstractType.Param;
import com.ethereal.server.Core.Manager.AbstractType.AbstractType;
import com.ethereal.server.Core.Manager.Event.Annotation.AfterEvent;
import com.ethereal.server.Core.Manager.Event.Annotation.BeforeEvent;
import com.ethereal.server.Core.Manager.Event.Model.AfterEventContext;
import com.ethereal.server.Core.Manager.Event.Model.BeforeEventContext;
import com.ethereal.server.Core.Manager.Event.Model.EventContext;
import com.ethereal.server.Core.Manager.Event.Model.ExceptionEventContext;
import com.ethereal.server.Core.Model.*;
import com.ethereal.server.Request.Annotation.InvokeTypeFlags;
import com.ethereal.server.Request.Annotation.RequestMapping;
import com.ethereal.server.Service.Abstract.Token;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

public class RequestMethodInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Request instance = (Request) o;
        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
        Object localResult = null;
        EventContext eventContext;
        Parameter[] parameterInfos = method.getParameters();
        ServerRequestModel request = new ServerRequestModel();
        request.setMapping(annotation.mapping());
        request.setParams(new HashMap<>(parameterInfos.length -1 ));
        HashMap<String,Object> params = new HashMap<>(parameterInfos.length);
        Token token = null;
        int idx = 0;
        for(Parameter parameterInfo : parameterInfos){
            if(parameterInfo.getAnnotation(com.ethereal.server.Service.Annotation.Token.class) != null){
                token = (Token) args[idx];
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
                com.ethereal.server.Core.Manager.Event.Annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.server.Core.Manager.Event.Annotation.ExceptionEvent.class);
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
            if(token != null){
                if(!token.getCanRequest()){
                    throw new TrackException(TrackException.ErrorCode.Runtime, String.format("{%s}-{%s}传递了无法请求的Token！", instance.name,annotation.mapping()));
                }
                token.sendServerRequest(request);
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
