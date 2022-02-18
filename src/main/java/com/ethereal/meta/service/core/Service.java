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
import com.ethereal.meta.service.annotation.*;
import com.ethereal.meta.service.event.InterceptorEvent;
import com.ethereal.meta.service.event.delegate.InterceptorDelegate;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;


@ServiceAnnotation
public abstract class Service implements IService {
    @Getter
    private ServiceConfig serviceConfig;
    @Getter
    private final HashMap<String,Method> methods = new HashMap<>();
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
                        methods.put(serviceMapping.getMapping(), method);
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
    public Object receive(ServiceContext context) {
        RequestMeta requestMeta = context.getRequestMeta();
        try {
            if(context.getMappings().isEmpty()){
                return new ResponseMeta(new Error(Error.ErrorCode.NotFoundMethod, String.format("Mapping:%s 未找到",requestMeta.getMapping())));
            }
            Meta nextMeta = this.meta.getMetas().get(context.getMappings().getFirst());
            if(nextMeta != null){
                context.getMappings().removeFirst();
                return nextMeta.getService().receive(context);
            }
            Method method = methods.get(context.getMappings().getFirst());
            if(method == null){
                return new ResponseMeta(new Error(Error.ErrorCode.NotFoundMethod, String.format("Mapping:%s 未找到",requestMeta.getMapping())));
            }

            context.setInstance(meta.newInstance(context.getLocal(),new NodeAddress(context.getRequestMeta().getHost(),context.getRequestMeta().getPort())));

            if(onInterceptor(requestMeta)){
                context.setParams(new HashMap<>());
                Parameter[] parameterInfos = method.getParameters();
                for(Parameter parameterInfo : parameterInfos){
                    if(requestMeta.getParams().containsKey(parameterInfo.getName())){
                        String value = requestMeta.getParams().get(parameterInfo.getName());
                        AbstractType type = meta.getTypes().get(parameterInfo);
                        context.getParams().put(parameterInfo.getName(),type.deserialize(value));
                    }
                    else return new ResponseMeta(new Error(Error.ErrorCode.NotFoundParam, String.format("%s实例中%s方法的%s参数未提供注入方案", meta.getPrefixes(),method.getName(),parameterInfo.getName())));
                }
                //Before
                beforeEvent(method,context);
                //Invoke
                Object localResult = null;
                try{
                    Object[] args = new Object[parameterInfos.length];
                    for (int i=0;i<parameterInfos.length;i++){
                        args[i] = context.getParams().get(parameterInfos[i].getName());
                    }
                    localResult = method.invoke(context.getInstance(),args);
                }
                catch (Exception e){
                    exceptionEvent(method,context,e);
                }
                //After
                afterEvent(method,context,localResult);
                //Return
                Class<?> return_type = method.getReturnType();
                if(return_type != void.class){
                    Param paramAnnotation = method.getAnnotation(Param.class);
                    AbstractType type = null;
                    if(paramAnnotation != null) type = meta.getTypes().getTypesByName().get(paramAnnotation.name());
                    if(type == null)type = meta.getTypes().getTypesByType().get(return_type);
                    if(type == null)return new ResponseMeta(new Error(Error.ErrorCode.NotFoundAbstractType,String.format("RPC中的%s类型参数尚未被注册！",return_type),null));
                    return new ResponseMeta(meta.serialize(context.getInstance()),type.serialize(localResult));
                }
                else return new ResponseMeta(meta.serialize(context.getInstance()),null);
            }
            else return new ResponseMeta(new Error(com.ethereal.meta.core.entity.Error.ErrorCode.Intercepted,"请求已被拦截",null));        }
        catch (Exception e){
            return new ResponseMeta(new Error(Error.ErrorCode.RemoteException, String.format("%s\n%s",e.getMessage(), Arrays.toString(e.getStackTrace()))));
        }
    }
    private void afterEvent(Method method,ServiceContext context,Object localResult) throws TrackException, InvocationTargetException, IllegalAccessException {
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            EventContext eventContext = new AfterEventContext(context.getParams(),method,localResult);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), afterEvent.function(), context.getParams(),eventContext);
        }
    }
    private void beforeEvent(Method method,ServiceContext context) throws TrackException, InvocationTargetException, IllegalAccessException {
        BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
        if(beforeEvent != null){
            EventContext eventContext = new BeforeEventContext(context.getParams(),method);
            String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), beforeEvent.function(), context.getParams(),eventContext);
        }
    }
    private void exceptionEvent(Method method, ServiceContext context, Exception e) throws Exception {
        com.ethereal.meta.core.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.meta.core.aop.annotation.ExceptionEvent.class);
        if(exceptionEvent != null){
            ExceptionEventContext eventContext = new ExceptionEventContext(context.getParams(),method,e);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            meta.getEventManager().invokeEvent(meta.getInstanceManager().get(iocObjectName), exceptionEvent.function(),context.getParams(),eventContext);
            if(exceptionEvent.isThrow())throw e;
        }
        else throw e;
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
