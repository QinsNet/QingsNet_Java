package com.ethereal.server.Service.Abstract;

import com.ethereal.server.Core.Annotation.BaseParam;
import com.ethereal.server.Core.BaseCore.MZCore;
import com.ethereal.server.Core.Manager.AbstractType.Param;
import com.ethereal.server.Core.Manager.AbstractType.AbstractTypeManager;
import com.ethereal.server.Core.Manager.AbstractType.AbstractType;
import com.ethereal.server.Core.Manager.Event.Annotation.AfterEvent;
import com.ethereal.server.Core.Manager.Event.Annotation.BeforeEvent;
import com.ethereal.server.Core.Manager.Event.Model.AfterEventContext;
import com.ethereal.server.Core.Manager.Event.Model.BeforeEventContext;
import com.ethereal.server.Core.Manager.Event.Model.EventContext;
import com.ethereal.server.Core.Manager.Event.Model.ExceptionEventContext;
import com.ethereal.server.Core.Model.*;
import com.ethereal.server.Core.Model.Error;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Request.Annotation.RequestMapping;
import com.ethereal.server.Server.Delegate.CreateInstanceDelegate;
import com.ethereal.server.Service.Annotation.ServiceMapping;
import com.ethereal.server.Service.EventRegister.Delegate.InterceptorDelegate;
import com.ethereal.server.Service.EventRegister.InterceptorEvent;
import com.ethereal.server.Service.Interface.IService;
import com.ethereal.server.Utils.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
@com.ethereal.server.Service.Annotation.Service
public abstract class Service extends MZCore implements IService {
    protected HashMap<String,Method> methods = new HashMap<>();
    protected Net net;
    protected String name;
    protected ServiceConfig config;
    protected InterceptorEvent interceptorEvent = new InterceptorEvent();
    protected HashMap<String, Request> requests = new HashMap<>();
    protected HashMap<Object, Token> tokens = new HashMap<>();
    protected Boolean enable;
    protected CreateInstanceDelegate createMethod;

    public CreateInstanceDelegate getCreateMethod() {
        return createMethod;
    }

    public void setCreateMethod(CreateInstanceDelegate createMethod) {
        this.createMethod = createMethod;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public HashMap<String, Request> getRequests() {
        return requests;
    }

    public void setRequests(HashMap<String, Request> requests) {
        this.requests = requests;
    }

    public HashMap<Object, Token> getTokens() {
        return tokens;
    }

    public void setTokens(HashMap<Object, Token> tokens) {
        this.tokens = tokens;
    }

    public Net getNet() {
        return net;
    }

    public void setNet(Net net) {
        this.net = net;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InterceptorEvent getInterceptorEvent() {
        return interceptorEvent;
    }

    public void setInterceptorEvent(InterceptorEvent interceptorEvent) {
        this.interceptorEvent = interceptorEvent;
    }

    public HashMap<String, Method> getMethods() {
        return methods;
    }
    public void setMethods(HashMap<String, Method> methods) {
        this.methods = methods;
    }

    public ServiceConfig getConfig() {
        return config;
    }

    public void setConfig(ServiceConfig config) {
        this.config = config;
    }

    public boolean OnInterceptor(Net net,Method method, Token token)
    {
        if (interceptorEvent != null)
        {
            for (InterceptorDelegate item : interceptorEvent.getListeners())
            {
                if (!item.onInterceptor(net,this, method, token)) return false;
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

    public ClientResponseModel clientRequestReceiveProcess(Token token, ClientRequestModel request) {
        try {
            Method method = getMethods().get(request.getMapping());
            if(method!= null){
                if(net.OnInterceptor(this,method,token) && OnInterceptor(net,method,token)){
                    EventContext eventContext;
                    Parameter[] parameterInfos = method.getParameters();
                    HashMap<String, Object> params = new HashMap<>(parameterInfos.length);
                    Object[] args = new Object[parameterInfos.length];
                    int idx = 0;
                    for(Parameter parameterInfo : parameterInfos){
                        if(parameterInfo.getAnnotation(com.ethereal.server.Service.Annotation.Token.class) != null){
                            args[idx] = token;
                        }
                        else if(request.getParams().containsKey(parameterInfo.getName())){
                            String value = request.getParams().get(parameterInfo.getName());
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
                        com.ethereal.server.Core.Manager.Event.Annotation.ExceptionEvent exceptionEvent = method.getAnnotation(com.ethereal.server.Core.Manager.Event.Annotation.ExceptionEvent.class);
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
                        if(type == null)return new ClientResponseModel(null,request.getId(),new Error(Error.ErrorCode.NotFoundAbstractType,String.format("RPC中的%s类型参数尚未被注册！",return_type),null));
                        return new ClientResponseModel(type.getSerialize().Serialize(localResult),request.getId(),null);
                    }
                    else return null;
                }
                else return new ClientResponseModel(null,request.getId(),new com.ethereal.server.Core.Model.Error(com.ethereal.server.Core.Model.Error.ErrorCode.Intercepted,"请求已被拦截",null));
            }
            else return new ClientResponseModel(null,request.getId(),new com.ethereal.server.Core.Model.Error(com.ethereal.server.Core.Model.Error.ErrorCode.Intercepted, String.format("未找到方法%s-%s-%s",net.getName(),name,request.getMapping() ),null));
        }
        catch (Exception e){
            return new ClientResponseModel(null,request.getId(),new com.ethereal.server.Core.Model.Error(Error.ErrorCode.Intercepted, String.format("%s\n%s",e.getMessage(), Arrays.toString(e.getStackTrace()))));
        }
    }
}
