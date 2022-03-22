package com.qins.net.request.core;

import com.qins.net.core.aop.annotation.AfterEvent;
import com.qins.net.core.aop.annotation.BeforeEvent;
import com.qins.net.core.aop.annotation.ExceptionEvent;
import com.qins.net.core.aop.context.AfterEventContext;
import com.qins.net.core.aop.context.BeforeEventContext;
import com.qins.net.core.aop.context.EventContext;
import com.qins.net.core.aop.context.ExceptionEventContext;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.core.exception.*;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.method.MethodPact;
import com.qins.net.meta.core.*;
import com.qins.net.meta.standard.StandardMetaSerialize;
import com.qins.net.node.core.Node;
import com.qins.net.request.aop.annotation.FailEvent;
import com.qins.net.request.aop.context.FailEventContext;
import com.qins.net.request.aop.annotation.SuccessEvent;
import com.qins.net.request.aop.annotation.TimeoutEvent;
import com.qins.net.request.aop.context.ErrorEventContext;
import com.qins.net.request.aop.context.SuccessEventContext;
import com.qins.net.request.aop.context.TimeoutEventContext;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class Request implements IRequest {
    @Getter
    protected final HashMap<String, MetaMethod> methods = new HashMap<>();
    @Getter
    protected final ConcurrentHashMap<String, RequestMeta> tasks = new ConcurrentHashMap<>();
    @Getter
    protected RequestConfig config = new RequestConfig();
    @Getter
    protected MetaClass metaClass;

    public Request(MetaClass metaClass){
        try {
            this.metaClass = metaClass;
            for (Method method: AnnotationUtil.getMetaMethods(metaClass.getInstanceClass())){
                if((method.getModifiers() & Modifier.ABSTRACT) == 0)continue;
                MetaMethod metaMethod = metaClass.getComponents().metaMethod().getConstructor(Method.class, Components.class).newInstance(method,metaClass.getComponents());
                methods.put(metaMethod.getName(), metaMethod);
                if(metaMethod.getNodes().size() == 0){
                    metaMethod.getNodes().addAll(metaClass.getDefaultNodes());
                }
            }

        }
        catch (Exception e){
            metaClass.onException(e);
        }
    }
    public NodeAddress searchNode(Map<String, String> nodes,MetaMethod metaMethod) throws NotFoundNodeException {
        //未来在这里配置注册中心系统
        for (String node : metaMethod.getNodes()){
            if(nodes.containsKey(node)){
                return new NodeAddress(nodes.get(node));
            }
        }
        throw new NotFoundNodeException(metaClass.getName(),metaMethod.getName());
    }

    public Object intercept(Object instance, Method method, Object[] args, Map<String, String> nodes) throws Exception {
        RequestContext context = new RequestContext();
        try {
            MethodPact pact = AnnotationUtil.getMethodPact(method);
            assert pact != null;
            MetaMethod metaMethod = methods.get(pact.getName());
            Object result = null;
            context.setInstance(instance)
                    .setReferencesContext(new ReferencesContext())
                    .setMetaMethod(metaMethod)
                    .setParams(new HashMap<>())
                    .setRemote(searchNode(nodes,metaMethod))
                    .setRequestMeta(buildRequestMeta(context,instance,args));

            beforeEvent(method,context);
            Node sender = metaMethod.getNodeClass().newInstance();
            sender.setMetaClass(metaClass);
            sender.setContext(context);
            int timeout = config.getTimeout();
            if(metaMethod.getTimeout() != -1)timeout = metaMethod.getTimeout();
            if(sender.send(context.getRequestMeta(),timeout)){
                synchronized (context){
                    context.wait();
                }
                ResponseMeta responseMeta = context.getResponseMeta();
                if(responseMeta != null){
                    result = context.getResult();
                    successEvent(method,context, responseMeta);
                }
                timeoutEvent(method,context);
            }
            else failEvent(method,context);
            afterEvent(method,context,result);
            return result;
        }
        catch (Exception e){
            exceptionEvent(method,context,e);
        }
        catch (Throwable e){
            exceptionEvent(method,context,new Exception("Request执行异常",e));
        }
        return null;
    }

    public void receive(RequestContext context) {
        try {
            if(context.getResponseMeta().getException() != null){
                errorEvent(context.getMetaMethod().getMethod(),context, context.getResponseMeta().getException());
            }
            handleResponseMeta(context);
            synchronized (context){
                context.notify();
            }
        }
        catch (Exception e){
            metaClass.onException(e);
        }
    }
    public void handleResponseMeta(RequestContext context) throws NotFoundInstanceFieldException, DeserializeException, IllegalAccessException, NotFoundParameterException {
        //引用池
        context.getReferencesContext().setDeserializePools(context.getResponseMeta().getReferences());
        //实例
        for (MetaField metaField : metaClass.getSyncFields().values()){
            Object rawField = context.getResponseMeta().getInstance().get(metaField.getName());
            Object field = StandardMetaSerialize.deserialize(rawField, context.getReferencesContext());
            metaField.getField().set(context.getInstance(),field);
        }
        //参数
        for (MetaParameter metaParameter : context.getMetaMethod().getSyncParameters().values()){
            Object rawParam = context.getResponseMeta().getParams().get(metaParameter.getName());
            StandardMetaSerialize.deserialize(rawParam,context.getReferencesContext());
        }
        //返回值
        if(context.getResponseMeta().getResult() != null){
            Object result = StandardMetaSerialize.deserialize(context.getResponseMeta().getResult(), context.getReferencesContext());
            context.setResult(result);
        }
    }
    public RequestMeta buildRequestMeta(RequestContext context, Object instance, Object[] args) throws SerializeException, IllegalAccessException {
        RequestMeta requestMeta = new RequestMeta();
        requestMeta.setMapping(metaClass.getName() + "/" + context.getMetaMethod().getName());
        //实例
        requestMeta.setInstance(new HashMap<>());
        for (MetaField metaField : metaClass.getFields().values()){
            Object field = metaField.getField().get(instance);
            requestMeta.getInstance().put(metaField.getName(),StandardMetaSerialize.serialize(field,context.getReferencesContext()));
        }
        //参数
        requestMeta.setParams(new HashMap<>());
        int i = 0;
        for (Map.Entry<String,MetaParameter> keyValue : context.getMetaMethod().getParameters().entrySet()){
            String name = keyValue.getKey();
            context.getParams().put(name,args[i]);
            context.getRequestMeta().getParams().put(name,StandardMetaSerialize.serialize(args[i],context.getReferencesContext()));
            i++;
        }
        //缓冲池
        requestMeta.setReferences(context.getReferencesContext().getSerializePools());
        return requestMeta;
    }

    protected void afterEvent(Method method, RequestContext context, Object result) throws TrackException, InvocationTargetException, IllegalAccessException {
        AfterEvent afterEvent = method.getAnnotation(AfterEvent.class);
        if(afterEvent != null){
            EventContext eventContext = new AfterEventContext(context.getParams(),method, result);
            String iocObjectName = afterEvent.function().substring(0,afterEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), afterEvent.function(), context.getParams(),eventContext);
        }
    }
    protected void beforeEvent(Method method,RequestContext context) throws TrackException, InvocationTargetException, IllegalAccessException {
        BeforeEvent beforeEvent = method.getAnnotation(BeforeEvent.class);
        if(beforeEvent != null){
            EventContext eventContext = new BeforeEventContext(context.getParams(),method);
            String iocObjectName = beforeEvent.function().substring(0, beforeEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), beforeEvent.function(), context.getParams(),eventContext);
        }
    }
    protected void exceptionEvent(Method method, RequestContext context, Exception e) throws Exception {
        ExceptionEvent exceptionEvent = method.getAnnotation(ExceptionEvent.class);
        if(exceptionEvent != null){
            ExceptionEventContext eventContext = new ExceptionEventContext(context.getParams(),method,e);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), exceptionEvent.function(),context.getParams(),eventContext);
            if(exceptionEvent.isThrow())throw e;
        }
        else throw e;
    }
    protected void errorEvent(Method method, RequestContext context, String exception) throws Exception {
        com.qins.net.request.aop.annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.qins.net.request.aop.annotation.ExceptionEvent.class);
        if(exceptionEvent != null){
            ErrorEventContext eventContext = new ErrorEventContext(context.getParams(),method, exception);
            String iocObjectName = exceptionEvent.function().substring(0, exceptionEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), exceptionEvent.function(), context.getParams(),eventContext);
            if(exceptionEvent.isThrow()){
                throw new TrackException(TrackException.ExceptionCode.ResponseException,exception);
            }
        }
        else throw new TrackException(TrackException.ExceptionCode.ResponseException,exception);
    }
    protected void successEvent(Method method, RequestContext context,ResponseMeta responseMeta) throws Exception {
        SuccessEvent successEvent = method.getAnnotation(SuccessEvent.class);
        if(successEvent != null){
            SuccessEventContext eventContext = new SuccessEventContext(context.getParams(),method,responseMeta.getResult());
            String iocObjectName = successEvent.function().substring(0, successEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), successEvent.function(),context.getParams(),eventContext);
        }
    }
    protected void timeoutEvent(Method method, RequestContext context) throws Exception {
        TimeoutEvent timeoutEvent =  method.getAnnotation(TimeoutEvent.class);
        if(timeoutEvent != null){
            TimeoutEventContext eventContext = new TimeoutEventContext(context.getParams(),method);
            String iocObjectName = timeoutEvent.function().substring(0, timeoutEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), timeoutEvent.function(),context.getParams(),eventContext);
        }
    }
    protected void failEvent(Method method, RequestContext context) throws Exception {
        FailEvent timeoutEvent =  method.getAnnotation(FailEvent.class);
        if(timeoutEvent != null){
            FailEventContext eventContext = new FailEventContext(context.getParams(),method);
            String iocObjectName = timeoutEvent.function().substring(0, timeoutEvent.function().indexOf("."));
            metaClass.getEventManager().invokeEvent(metaClass.getInstanceManager().get(iocObjectName), timeoutEvent.function(),context.getParams(),eventContext);
        }
    }
}
