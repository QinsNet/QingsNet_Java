package com.ethereal.net.service.core;

import com.ethereal.net.core.annotation.BaseParam;
import com.ethereal.net.core.base.MZCore;
import com.ethereal.net.core.manager.type.Param;
import com.ethereal.net.core.manager.type.AbstractType;
import com.ethereal.net.core.manager.event.Annotation.AfterEvent;
import com.ethereal.net.core.manager.event.Annotation.BeforeEvent;
import com.ethereal.net.core.manager.event.Model.AfterEventContext;
import com.ethereal.net.core.manager.event.Model.BeforeEventContext;
import com.ethereal.net.core.manager.event.Model.EventContext;
import com.ethereal.net.core.manager.event.Model.ExceptionEventContext;
import com.ethereal.net.core.entity.*;
import com.ethereal.net.core.entity.Error;
import com.ethereal.net.net.core.Net;
import com.ethereal.net.node.core.Node;
import com.ethereal.net.service.annotation.ServiceMapping;
import com.ethereal.net.service.event.delegate.InterceptorDelegate;
import com.ethereal.net.service.event.InterceptorEvent;
import com.ethereal.net.service.annotation.IService;
import com.ethereal.net.utils.AnnotationUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

@Getter
@Setter
@com.ethereal.net.service.annotation.Service
public abstract class Service extends MZCore implements IService {
    protected HashMap<String,Method> methods = new HashMap<>();
    protected Service parent;
    protected String name;
    protected ServiceConfig config;
    protected InterceptorEvent interceptorEvent = new InterceptorEvent();
    protected HashMap<String,Service> services = new HashMap<>();
    protected HashMap<Object, Node> tokens = new HashMap<>();
    protected Boolean enable;
    public abstract Node createNode(RequestMeta requestMeta);
    public boolean onInterceptor(RequestMeta requestMeta)
    {
        if (interceptorEvent != null)
        {
            for (InterceptorDelegate item : interceptorEvent.getListeners())
            {
                if (!item.onInterceptor(requestMeta)) return false;
            }
            return true;
        }
        else return true;
    }

    public static void register(Service instance) throws TrackException {
        for (Method method : instance.getClass().getMethods()){
            ServiceMapping requestAnnotation = method.getAnnotation(ServiceMapping.class);
            if(requestAnnotation !=null){
                if(method.getReturnType() != void.class){
                    Param paramAnnotation = method.getAnnotation(Param.class);
                    if(paramAnnotation != null){
                        String typeName = paramAnnotation.type();
                        if(instance.getTypes().get(typeName) == null){
                            throw new TrackException(TrackException.ErrorCode.Core, String.format("%s 未提供 %s 抽象类型的映射", method.getName(),typeName));
                        }
                    }
                    else if(instance.getTypes().get(method.getReturnType()) == null){
                        throw new TrackException(TrackException.ErrorCode.Core, String.format("%s 返回值未提供 %s 类型的抽象映射", method.getName(),method.getReturnType()));
                    }
                }
                for (Parameter parameter : method.getParameters()){
                    if(AnnotationUtils.getAnnotation(parameter,BaseParam.class) != null){
                        continue;
                    }
                    Param paramAnnotation = method.getAnnotation(Param.class);
                    if(paramAnnotation != null){
                        String typeName = paramAnnotation.type();
                        if(instance.getTypes().get(typeName) == null){
                            throw new TrackException(TrackException.ErrorCode.Core, String.format("%s-%s-%s抽象类型未找到",instance.getName() ,method.getName(),paramAnnotation.type()));
                        }
                    }
                    else if(instance.getTypes().get(parameter.getParameterizedType()) == null){
                        throw new TrackException(TrackException.ErrorCode.Core, String.format("%s-%s-%s类型映射抽象类型",instance.getName() ,method.getName(),parameter.getParameterizedType()));
                    }
                }
                instance.methods.put(requestAnnotation.mapping(),method);
            }
        }
    }

    public ResponseMeta receiveProcess(RequestMeta requestMeta) {
        try {
            Method method = requestMeta.getMethod();
            if(onInterceptor(requestMeta)){
                EventContext eventContext;
                Parameter[] parameterInfos = method.getParameters();
                HashMap<String, Object> params = new HashMap<>(parameterInfos.length);
                Object[] args = new Object[parameterInfos.length];
                int idx = 0;
                for(Parameter parameterInfo : parameterInfos){
                    if(parameterInfo.getAnnotation(com.ethereal.net.node.annotation.Node.class) != null){
                        args[idx] = requestMeta.getNode();
                    }
                    else if(requestMeta.getParams().containsKey(parameterInfo.getName())){
                        String value = requestMeta.getParams().get(parameterInfo.getName());
                        AbstractType type = getTypes().get(parameterInfo);
                        args[idx] = type.getDeserialize().Deserialize(value);
                    }
                    else throw new TrackException(TrackException.ErrorCode.Runtime,
                                String.format("%s实例中%s方法的%s参数未提供注入方案",name,method.getName(),parameterInfo.getName()));
                    params.put(parameterInfo.getName(), args[idx++]);
                }
                BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
                if(beforeEvent != null){
                    eventContext = new BeforeEventContext(params,method);
                    String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
                    iocManager.invokeEvent(iocManager.get(iocObjectName), beforeEvent.function(), params,eventContext);
                }
                Object localResult = null;
                try{
                    localResult = method.invoke(this,args);
                }
                catch (Exception e){
                    com.ethereal.net.core.manager.event.Annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.net.core.manager.event.Annotation.ExceptionEvent.class);
                    if(exceptionEvent != null){
                        eventContext = new ExceptionEventContext(params,method,e);
                        String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
                        iocManager.invokeEvent(iocManager.get(iocObjectName), exceptionEvent.function(),params,eventContext);
                        if(exceptionEvent.isThrow())throw e;
                    }
                    else throw e;
                }
                AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
                if(afterEvent != null){
                    eventContext = new AfterEventContext(params,method,localResult);
                    String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
                    iocManager.invokeEvent(iocManager.get(iocObjectName), afterEvent.function(), params,eventContext);
                }
                Class<?> return_type = method.getReturnType();
                if(return_type != void.class){
                    Param paramAnnotation = method.getAnnotation(Param.class);
                    AbstractType type = null;
                    if(paramAnnotation != null) type = getTypes().getTypesByName().get(paramAnnotation.type());
                    if(type == null)type = getTypes().getTypesByType().get(return_type);
                    if(type == null)return new ResponseMeta(null, requestMeta.getId(),new Error(Error.ErrorCode.NotFoundAbstractType,String.format("RPC中的%s类型参数尚未被注册！",return_type),null));
                    return new ResponseMeta(type.getSerialize().Serialize(localResult), requestMeta.getId(),null);
                }
                else return null;
            }
            else return new ResponseMeta(null, requestMeta.getId(),new com.ethereal.net.core.entity.Error(com.ethereal.net.core.entity.Error.ErrorCode.Intercepted,"请求已被拦截",null));        }
        catch (Exception e){
            return new ResponseMeta(null, requestMeta.getId(),new com.ethereal.net.core.entity.Error(Error.ErrorCode.Exception, String.format("%s\n%s",e.getMessage(), Arrays.toString(e.getStackTrace()))));
        }
    }


    public static boolean unregister(Service service) throws TrackException {
        if(service.getRegister()){
            service.unregister();
            service.getNet().getServices().remove(service.getName());
            service.setNet(null);
            service.unInitialize();
            service.setRegister(false);
            return true;
        }
        else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s已经UnRegister,无法重复UnRegister", service.getName()));
    }
}
