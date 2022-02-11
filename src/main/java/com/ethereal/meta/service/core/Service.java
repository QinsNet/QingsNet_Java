package com.ethereal.meta.service.core;

import com.ethereal.meta.core.type.Param;
import com.ethereal.meta.core.type.AbstractType;
import com.ethereal.meta.core.aop.annotation.AfterEvent;
import com.ethereal.meta.core.aop.annotation.BeforeEvent;
import com.ethereal.meta.core.aop.context.AfterEventContext;
import com.ethereal.meta.core.aop.context.BeforeEventContext;
import com.ethereal.meta.core.aop.context.EventContext;
import com.ethereal.meta.core.aop.context.ExceptionEventContext;
import com.ethereal.meta.core.entity.*;
import com.ethereal.meta.core.entity.Error;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.request.annotation.*;
import com.ethereal.meta.service.annotation.*;
import com.ethereal.meta.service.event.InterceptorEvent;
import com.ethereal.meta.service.event.delegate.InterceptorDelegate;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;


@ServiceAnnotation
public abstract class Service implements IService {
    @Getter
    private ServiceConfig serviceConfig;
    @Getter
    private final HashMap<String,Method> services = new HashMap<>();
    @Getter
    private Meta meta;
    @Getter
    protected final InterceptorEvent interceptorEvent = new InterceptorEvent();

    public Service(Meta meta){
        try {
            this.meta = meta;
            Class<?> checkClass = meta.getInstanceClass();
            while (checkClass != null){
                for (Method method : checkClass.getDeclaredMethods()){
                    ServiceMapping serviceMapping = getServiceMapping(method);
                    if(serviceMapping !=null){
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
                                        throw new TrackException(TrackException.ExceptionCode.NotFoundType, String.format("%s-%s-%s抽象类型未找到", meta.getInstanceClass().getName() ,method.getName(),paramAnnotation.name()));
                                    }
                                }
                            }
                            else if(meta.getTypes().get(parameter.getParameterizedType()) == null){
                                meta.getTypes().add(parameter.getName(),parameter.getType());
                            }
                        }
                        services.put(serviceMapping.getMapping(), method);
                    }
                }
                checkClass = checkClass.getSuperclass();
            }
        }
        catch (Exception e){
            meta.onException(e);
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
    public Object receive(RequestMeta requestMeta) {
        try {
            Method method = null;
            LinkedList<String> mappings = new LinkedList<>(Arrays.asList(requestMeta.getMapping().split("/")));
            if(services.containsKey(mappings.getLast())){
               method = services.get(mappings.getLast());
            }
            else {
                return new ResponseMeta(requestMeta,new Error(Error.ErrorCode.NotFoundMethod, String.format("Mapping:%s 未找到",requestMeta.getMapping())));
            }
            if(onInterceptor(requestMeta)){
                EventContext eventContext;
                Parameter[] parameterInfos = method.getParameters();
                for(Parameter parameterInfo : parameterInfos){
                    if(requestMeta.getRawParams().containsKey(parameterInfo.getName())){
                        String value = requestMeta.getRawParams().get(parameterInfo.getName());
                        AbstractType type = meta.getTypes().get(parameterInfo);
                        requestMeta.getParams().put(parameterInfo.getName(),type.deserialize(value));
                    }
                    else return new ResponseMeta(requestMeta,new Error(Error.ErrorCode.NotFoundParam, String.format("%s实例中%s方法的%s参数未提供注入方案", getClass().getName(),method.getName(),parameterInfo.getName())));
                }
                BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
                if(beforeEvent != null){
                    eventContext = new BeforeEventContext(requestMeta.getParams(),method);
                    String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
                    meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), beforeEvent.function(), requestMeta.getParams(),eventContext);
                }
                Object localResult = null;
                try{
                    Object[] args = new Object[parameterInfos.length];
                    for (int i=0;i<parameterInfos.length;i++){
                        args[i] = requestMeta.getParams().get(parameterInfos[i].getName());
                    }
                    localResult = method.invoke(this,args);
                }
                catch (Exception e){
                    com.ethereal.meta.core.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.meta.core.aop.annotation.ExceptionEvent.class);
                    if(exceptionEvent != null){
                        eventContext = new ExceptionEventContext(requestMeta.getParams(),method,e);
                        String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
                        meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), exceptionEvent.function(),requestMeta.getParams(),eventContext);
                        if(exceptionEvent.isThrow())throw e;
                    }
                    else throw e;
                }
                AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
                if(afterEvent != null){
                    eventContext = new AfterEventContext(requestMeta.getParams(),method,localResult);
                    String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
                    meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), afterEvent.function(), requestMeta.getParams(),eventContext);
                }
                Class<?> return_type = method.getReturnType();
                if(return_type != void.class){
                    Param paramAnnotation = method.getAnnotation(Param.class);
                    AbstractType type = null;
                    if(paramAnnotation != null) type = meta.getTypes().getTypesByName().get(paramAnnotation.name());
                    if(type == null)type = meta.getTypes().getTypesByType().get(return_type);
                    if(type == null)return new ResponseMeta(requestMeta,new Error(Error.ErrorCode.NotFoundAbstractType,String.format("RPC中的%s类型参数尚未被注册！",return_type),null));
                    return new ResponseMeta(requestMeta,type.serialize(localResult));
                }
                else return null;
            }
            else return new ResponseMeta(requestMeta,new Error(com.ethereal.meta.core.entity.Error.ErrorCode.Intercepted,"请求已被拦截",null));        }
        catch (Exception e){
            return new ResponseMeta(requestMeta,new Error(Error.ErrorCode.Exception, String.format("%s\n%s",e.getMessage(), Arrays.toString(e.getStackTrace()))));
        }
    }
    public ServiceMapping getServiceMapping(Method method){
        if(method.getAnnotation(PostService.class) != null){
            ServiceMapping serviceMapping = new ServiceMapping();
            PostService annotation = method.getAnnotation(PostService.class);
            serviceMapping.setMapping(annotation.value());
            serviceMapping.setTimeout(annotation.timeout());
            serviceMapping.setMethod(ServiceType.Post);
            return serviceMapping;
        }
        else if(method.getAnnotation(GetService.class) != null){
            ServiceMapping serviceMapping = new ServiceMapping();
            GetService annotation = method.getAnnotation(GetService.class);
            serviceMapping.setMapping(annotation.value());
            serviceMapping.setTimeout(annotation.timeout());
            serviceMapping.setMethod(ServiceType.Get);
            return serviceMapping;
        }
        else if(method.getAnnotation(MetaService.class) != null){
            ServiceMapping serviceMapping = new ServiceMapping();
            MetaService annotation = method.getAnnotation(MetaService.class);
            serviceMapping.setMapping(annotation.value());
            serviceMapping.setTimeout(annotation.timeout());
            serviceMapping.setMethod(ServiceType.Command);
            return serviceMapping;
        }
        return null;
    }
}
