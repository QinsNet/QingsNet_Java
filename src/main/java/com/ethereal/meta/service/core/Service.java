package com.ethereal.meta.service.core;

import com.ethereal.meta.core.annotation.BaseParam;
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
import com.ethereal.meta.meta.RawMeta;
import com.ethereal.meta.service.annotation.ServiceMapping;
import com.ethereal.meta.service.annotation.IService;
import com.ethereal.meta.utils.AnnotationUtils;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;


@com.ethereal.meta.service.annotation.Service
public abstract class Service extends RawMeta implements IService {
    @Getter
    private ServiceConfig serviceConfig;
    static Service() throws TrackException {
        for (Method method : Service.class.getMethods()){
            ServiceMapping requestAnnotation = method.getAnnotation(ServiceMapping.class);
            if(requestAnnotation !=null){
                if(method.getReturnType() != void.class){
                    Param paramAnnotation = method.getAnnotation(Param.class);
                    if(paramAnnotation != null){
                        String typeName = paramAnnotation.type();
                        if(getTypes().get(typeName) == null){
                            throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s 未提供 %s 抽象类型的映射", method.getName(),typeName));
                        }
                    }
                    else if(getTypes().get(method.getReturnType()) == null){
                        throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s 返回值未提供 %s 类型的抽象映射", method.getName(),method.getReturnType()));
                    }
                }
                for (Parameter parameter : method.getParameters()){
                    if(AnnotationUtils.getAnnotation(parameter,BaseParam.class) != null){
                        continue;
                    }
                    Param paramAnnotation = method.getAnnotation(Param.class);
                    if(paramAnnotation != null){
                        String typeName = paramAnnotation.type();
                        if(getTypes().get(typeName) == null){
                            throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s-%s-%s抽象类型未找到",getClass().getName() ,method.getName(),paramAnnotation.type()));
                        }
                    }
                    else if(getTypes().get(parameter.getParameterizedType()) == null){
                        throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s-%s-%s类型映射抽象类型",getClass().getName() ,method.getName(),parameter.getParameterizedType()));
                    }
                }
            }
        }
    }

    public Object receiveProcess(RequestMeta requestMeta) {
        try {
            Method method = requestMeta.getMethod();
            if(onInterceptor(requestMeta)){
                EventContext eventContext;
                Parameter[] parameterInfos = method.getParameters();
                HashMap<String, Object> params = new HashMap<>(parameterInfos.length);
                Object[] args = new Object[parameterInfos.length];
                int idx = 0;
                for(Parameter parameterInfo : parameterInfos){
                    if(parameterInfo.getAnnotation(com.ethereal.meta.node.annotation.Node.class) != null){
                        args[idx] = requestMeta.getNode();
                    }
                    else if(requestMeta.getParams().containsKey(parameterInfo.getName())){
                        String value = requestMeta.getParams().get(parameterInfo.getName());
                        AbstractType type = getTypes().get(parameterInfo);
                        args[idx] = type.getDeserialize().Deserialize(value);
                    }
                    else throw new TrackException(TrackException.ErrorCode.Runtime,
                                String.format("%s实例中%s方法的%s参数未提供注入方案",getClass().getName(),method.getName(),parameterInfo.getName()));
                    params.put(parameterInfo.getName(), args[idx++]);
                }
                BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
                if(beforeEvent != null){
                    eventContext = new BeforeEventContext(params,method);
                    String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
                    eventManager.invokeEvent(instanceManager.get(iocObjectName), beforeEvent.function(), params,eventContext);
                }
                Object localResult = null;
                try{
                    localResult = method.invoke(this,args);
                }
                catch (Exception e){
                    com.ethereal.meta.core.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.meta.core.aop.annotation.ExceptionEvent.class);
                    if(exceptionEvent != null){
                        eventContext = new ExceptionEventContext(params,method,e);
                        String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
                        eventManager.invokeEvent(instanceManager.get(iocObjectName), exceptionEvent.function(),params,eventContext);
                        if(exceptionEvent.isThrow())throw e;
                    }
                    else throw e;
                }
                AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
                if(afterEvent != null){
                    eventContext = new AfterEventContext(params,method,localResult);
                    String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
                    eventManager.invokeEvent(instanceManager.get(iocObjectName), afterEvent.function(), params,eventContext);
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
            else return new ResponseMeta(null, requestMeta.getId(),new com.ethereal.meta.core.entity.Error(com.ethereal.meta.core.entity.Error.ErrorCode.Intercepted,"请求已被拦截",null));        }
        catch (Exception e){
            return new ResponseMeta(null, requestMeta.getId(),new com.ethereal.meta.core.entity.Error(Error.ErrorCode.Exception, String.format("%s\n%s",e.getMessage(), Arrays.toString(e.getStackTrace()))));
        }
    }

//
//    public <T> T register(ServiceMeta serviceNet) throws TrackException {
//        if(!services.containsKey(serviceNet.name)){
//            serviceNet.setInitialized(true);
//            serviceNet.initialize();
//            serviceNet.setParent(this);
//            serviceNet.setPrefixes(serviceNet.getPrefixes() + this.getName());
//            serviceNet.getExceptionEvent().register(this::onException);
//            serviceNet.getLogEvent().register(this::onLog);
//            services.put(serviceNet.getName(), serviceNet);
//            return (T) serviceNet;
//        }
//        else throw new TrackException(TrackException.ErrorCode.Initialize,String.format("%s/%s已注册,无法重复注册！",prefixes, serviceNet.getName()));
//    }
//
//    public boolean unRegister() throws TrackException {
//        if(initialized){
//            unInitialize();
//            for(ServiceMeta serviceNet : services.values()){
//                serviceNet.unRegister();
//            }
//            getExceptionEvent().clear();
//            getLogEvent().clear();
//            parent.getServices().remove(name);
//            parent = null;
//            prefixes = null;
//            initialized = false;
//            return true;
//        }
//        else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s已经UnRegister,无法重复UnRegister", prefixes));
//    }
//
//    public Node publish() throws TrackException {
//        if(node.getNetwork() != null){
//            throw new TrackException(TrackException.ErrorCode.Initialize,String.format("%s已部署,无法重复部署！",prefixes));
//        }
//        node.setNetwork(new Http2Server(this));
//        node.getNetwork().start();
//        return node;
//    }
}
