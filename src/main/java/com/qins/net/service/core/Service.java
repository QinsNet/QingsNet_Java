package com.qins.net.service.core;

import com.qins.net.core.aop.annotation.AfterEvent;
import com.qins.net.core.aop.annotation.BeforeEvent;
import com.qins.net.core.aop.annotation.ExceptionEvent;
import com.qins.net.core.aop.context.AfterEventContext;
import com.qins.net.core.aop.context.BeforeEventContext;
import com.qins.net.core.aop.context.EventContext;
import com.qins.net.core.aop.context.ExceptionEventContext;
import com.qins.net.core.entity.*;
import com.qins.net.meta.annotation.MethodMapping;
import com.qins.net.meta.core.*;
import com.qins.net.service.event.InterceptorEvent;
import com.qins.net.service.event.delegate.InterceptorDelegate;
import com.qins.net.core.entity.ResponseException;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;


public abstract class Service implements IService {
    @Getter
    private ServiceConfig serviceConfig;
    @Getter
    private final HashMap<String,MetaMethod> methods = new HashMap<>();
    @Getter
    private MetaNodeField metaNodeField;
    @Getter
    protected final InterceptorEvent interceptorEvent = new InterceptorEvent();

    public Service(MetaNodeField metaNodeField){
        try {
            this.metaNodeField = metaNodeField;
            for (Method method: AnnotationUtil.getMethods(metaNodeField.getInstanceClass(), MethodMapping.class)){
                if(!method.isDefault())return;
                MetaMethod metaMethod = new MetaMethod();
                metaMethod.setMethod(method);
                metaMethod.setMethodPact(AnnotationUtil.getMethodPact(method));
                if(metaMethod.getMethodPact() != null && !method.isDefault()){
                    if(method.getReturnType() != void.class){
                        metaMethod.setMetaReturn(metaNodeField.getComponents().metaReturn().getConstructor(Class.class).newInstance(method.getReturnType()));
                    }
                    metaMethod.setMetaParameters(new HashMap<>());
                    for (Parameter parameter : method.getParameters()){
                        MetaParameter metaParameter = metaNodeField.getComponents().metaParameter()
                                .getConstructor(Parameter.class)
                                .newInstance(parameter);
                        metaMethod.getMetaParameters().put(metaParameter.getName(),metaParameter);
                    }
                    methods.put(metaMethod.getMethodPact().getMapping(), metaMethod);
                }
            }
        }
        catch (Exception e){
            metaNodeField.onException(e);
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
                throw new ResponseException(ResponseException.ExceptionCode.NotFoundMethod, String.format("Mapping:%s 未找到",requestMeta.getMapping()));
            }
            MetaNodeField metaNodeField = this.metaNodeField.getMetas().get(context.getMappings().getFirst());
            if(metaNodeField != null){
                context.getMappings().removeFirst();
                return metaNodeField.getService().receive(context);
            }
            MetaMethod metaMethod = methods.get(context.getMappings().getFirst());
            if(metaMethod == null){
                throw new ResponseException(ResponseException.ExceptionCode.NotFoundMethod, String.format("Mapping:%s 未找到",requestMeta.getMapping()));
            }
            context.setInstance(metaNodeField.newInstance(context.getRequestMeta().getInstance(),context.getLocal(),new NodeAddress(context.getRequestMeta().getHost(),context.getRequestMeta().getPort())));
            if(onInterceptor(requestMeta)){
                context.setParams(new HashMap<>());
                for (MetaParameter metaParameter : metaMethod.getMetaParameters().values()){
                    String rawParam = context.getRequestMeta().getParams().get(metaParameter.getName());
                    Object param = metaParameter.deserialize(rawParam);
                    context.getParams().put(metaParameter.getName(),param);
                }
                //Before
                beforeEvent(metaMethod.getMethod(),context);
                //Invoke
                Object localResult = null;
                try{
                    Object[] args = new Object[metaMethod.getMetaParameters().size()];
                    int i=0;
                    for (String name : metaMethod.getMetaParameters().keySet()){
                        args[i++] = context.getParams().get(name);
                    }
                    localResult = metaMethod.getMethod().invoke(context.getInstance(),args);
                }
                catch (Exception e){
                    exceptionEvent(metaMethod.getMethod(),context,e);
                }
                //After
                afterEvent(metaMethod.getMethod(),context,localResult);
                HashMap<String,String> syncParams = new HashMap<>();
                for(MetaParameter metaParameter : metaMethod.getMetaSyncParameters().values()){
                    Object param = context.getParams().get(metaParameter.getName());
                    syncParams.put(metaParameter.getName(),metaParameter.serialize(param));
                }
                //Return
                MetaReturn metaReturn = metaMethod.getMetaReturn();
                if(metaReturn != null){
                    return new ResponseMeta(metaNodeField.serialize(context.getInstance()),syncParams,metaReturn.serialize(localResult));
                }
                else return new ResponseMeta(metaNodeField.serialize(context.getInstance()),syncParams,null);
            }
            else throw new ResponseException(ResponseException.ExceptionCode.Intercepted,"请求已被拦截",null);
        }
        catch (Exception e){
            return new ResponseMeta(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }
    private void afterEvent(Method method,ServiceContext context,Object localResult) throws TrackException, InvocationTargetException, IllegalAccessException {
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            EventContext eventContext = new AfterEventContext(context.getParams(),method,localResult);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), afterEvent.function(), context.getParams(),eventContext);
        }
    }
    private void beforeEvent(Method method,ServiceContext context) throws TrackException, InvocationTargetException, IllegalAccessException {
        BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
        if(beforeEvent != null){
            EventContext eventContext = new BeforeEventContext(context.getParams(),method);
            String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), beforeEvent.function(), context.getParams(),eventContext);
        }
    }
    private void exceptionEvent(Method method, ServiceContext context, Exception e) throws Exception {
        ExceptionEvent exceptionEvent = method.getAnnotation(ExceptionEvent.class);
        if(exceptionEvent != null){
            ExceptionEventContext eventContext = new ExceptionEventContext(context.getParams(),method,e);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            metaNodeField.getEventManager().invokeEvent(metaNodeField.getInstanceManager().get(iocObjectName), exceptionEvent.function(),context.getParams(),eventContext);
            if(exceptionEvent.isThrow())throw e;
        }
        else throw e;
    }
}
