package com.ethereal.net.service.core;

import com.ethereal.net.core.annotation.BaseParam;
import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.manager.ioc.IocManager;
import com.ethereal.net.core.manager.type.AbstractTypeManager;
import com.ethereal.net.core.manager.type.Param;
import com.ethereal.net.core.manager.type.AbstractType;
import com.ethereal.net.core.manager.aop.annotation.AfterEvent;
import com.ethereal.net.core.manager.aop.annotation.BeforeEvent;
import com.ethereal.net.core.manager.aop.context.AfterEventContext;
import com.ethereal.net.core.manager.aop.context.BeforeEventContext;
import com.ethereal.net.core.manager.aop.context.EventContext;
import com.ethereal.net.core.manager.aop.context.ExceptionEventContext;
import com.ethereal.net.core.entity.*;
import com.ethereal.net.core.entity.Error;
import com.ethereal.net.node.core.Node;
import com.ethereal.net.node.network.http.server.Http2Server;
import com.ethereal.net.request.annotation.RequestMapping;
import com.ethereal.net.request.core.Request;
import com.ethereal.net.request.core.RequestInterceptor;
import com.ethereal.net.service.annotation.ServiceMapping;
import com.ethereal.net.service.event.InterceptorEvent;
import com.ethereal.net.service.annotation.IService;
import com.ethereal.net.service.event.delegate.InterceptorDelegate;
import com.ethereal.net.utils.AnnotationUtils;
import lombok.Getter;
import lombok.Setter;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;

@Getter
@Setter
@com.ethereal.net.service.annotation.Service
public abstract class Service extends BaseCore implements IService {
    protected HashMap<String,Method> methods = new HashMap<>();
    protected Service parent;
    protected String prefixes;
    protected ServiceConfig config;
    protected InterceptorEvent interceptorEvent = new InterceptorEvent();
    protected HashMap<String,Service> services = new HashMap<>();
    protected HashMap<Object,Node> nodes = new HashMap<>();
    protected Boolean initialized = false;
    protected Node node = createNode(null);
    @Getter
    protected String name;
    @Getter
    protected AbstractTypeManager types = new AbstractTypeManager();
    @Getter
    protected IocManager iocManager = new IocManager();

    public abstract Node createNode(String raw_node);
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

    public Service() throws TrackException {
        for (Method method : getClass().getMethods()){
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
                            throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s-%s-%s抽象类型未找到",getName() ,method.getName(),paramAnnotation.type()));
                        }
                    }
                    else if(getTypes().get(parameter.getParameterizedType()) == null){
                        throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s-%s-%s类型映射抽象类型",getName() ,method.getName(),parameter.getParameterizedType()));
                    }
                }
                methods.put(requestAnnotation.mapping(),method);
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
                    com.ethereal.net.core.manager.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.net.core.manager.aop.annotation.ExceptionEvent.class);
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

    
    public <T> T register(Service service) throws TrackException {
        if(!services.containsKey(service.name)){
            service.setInitialized(true);
            service.initialize();
            service.setParent(this);
            service.setPrefixes(service.getPrefixes() + this.getName());
            service.getExceptionEvent().register(this::onException);
            service.getLogEvent().register(this::onLog);
            services.put(service.getName(),service);
            return (T) service;
        }
        else throw new TrackException(TrackException.ErrorCode.Initialize,String.format("%s/%s已注册,无法重复注册！",prefixes,service.getName()));
    }

    public boolean unRegister() throws TrackException {
        if(initialized){
            unInitialize();
            for(Service service : services.values()){
                service.unRegister();
            }
            getExceptionEvent().clear();
            getLogEvent().clear();
            parent.getServices().remove(name);
            parent = null;
            prefixes = null;
            initialized = false;
            return true;
        }
        else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s已经UnRegister,无法重复UnRegister", prefixes));
    }

    public Node publish() throws TrackException {
        if(node.getNetwork() != null){
            throw new TrackException(TrackException.ErrorCode.Initialize,String.format("%s已部署,无法重复部署！",prefixes));
        }
        node.setNetwork(new Http2Server(this));
        node.getNetwork().start();
        return node;
    }
}
