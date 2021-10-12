package com.ethereal.server.Net.Abstract;

import com.ethereal.server.Core.Enums.NetType;
import com.ethereal.server.Core.Event.ExceptionEvent;
import com.ethereal.server.Core.Event.LogEvent;
import com.ethereal.server.Core.Model.*;
import com.ethereal.server.Core.Model.Error;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.Interface.INet;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Server.Abstract.Token;
import com.ethereal.server.Server.Abstract.Server;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Service.Event.Delegate.InterceptorDelegate;
import com.ethereal.server.Service.Event.InterceptorEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class Net implements INet {
    protected NetConfig config;
    protected String name;
    protected NetType netType;
    protected ExceptionEvent exceptionEvent = new ExceptionEvent();
    protected LogEvent logEvent = new LogEvent();
    protected Server server;
    protected HashMap<String, Service> services = new HashMap<>();
    protected HashMap<String, Request> requests = new HashMap<>();
    protected HashMap<Object, Token> tokens = new HashMap<>();
    protected InterceptorEvent interceptorEvent = new InterceptorEvent();
    public Net(String name){
        this.name = name;
    }
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public HashMap<Object, Token> getTokens() {
        return tokens;
    }

    public void setTokens(HashMap<Object, Token> tokens) {
        this.tokens = tokens;
    }

    public NetType getNetType() {
        return netType;
    }

    public void setNetType(NetType netType) {
        this.netType = netType;
    }

    public void setExceptionEvent(ExceptionEvent exceptionEvent) {
        this.exceptionEvent = exceptionEvent;
    }

    public void setLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public NetConfig getConfig() {
        return config;
    }

    public void setConfig(NetConfig config) {
        this.config = config;
    }

    public HashMap<String, Service> getServices() {
        return services;
    }

    public void setServices(HashMap<String, Service> services) {
        this.services = services;
    }

    public HashMap<String, Request> getRequests() {
        return requests;
    }

    public void setRequests(HashMap<String, Request> requests) {
        this.requests = requests;
    }

    public boolean OnInterceptor(Service service, Method method, Token token)
    {
        if (interceptorEvent != null)
        {
            for (InterceptorDelegate item : interceptorEvent.getListeners())
            {
                if (!item.onInterceptor(this,service, method, token)) return false;
            }
            return true;
        }
        else return true;
    }
    @Override
    public ClientResponseModel clientRequestReceiveProcess(Token token, ClientRequestModel request) {
        try {
            Method method;
            Service service = services.get(request.getService());
            if(service != null){
                method = service.getMethods().get(request.getMethodId());
                if(method!= null){
                    if(OnInterceptor(service,method,token) && service.OnInterceptor(this,method,token)){
                        Parameter[] parametersInfos = method.getParameters();
                        ArrayList<Object> parameters = new ArrayList<>(parametersInfos.length);
                        int i = 0;
                        for (Parameter parameterInfo : parametersInfos)
                        {
                            if(parameterInfo.getAnnotation(com.ethereal.server.Server.Annotation.Token.class)!=null){
                                parameters.add(token);
                            }
                            else {
                                AbstractType type;
                                type = service.getTypes().getTypesByType().get(parameterInfo.getParameterizedType());
                                if(type == null)type = service.getTypes().getTypesByName().get(parameterInfo.getAnnotation(com.ethereal.server.Core.Annotation.AbstractType.class).abstractName());
                                if(type == null)return new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundAbstractType,String.format("RPC中的%s类型参数尚未被注册！",parameterInfo.getParameterizedType()),null),request.getId(),request.getService());
                                parameters.add(type.getDeserialize().Deserialize(request.getParams()[i++]));
                            }
                        }
                        Object result = method.invoke(service,parameters.toArray(new Object[]{}));
                        Class<?> return_type = method.getReturnType();
                        if(return_type != Void.class){
                            AbstractType type = service.getTypes().getTypesByType().get(return_type);
                            if(type == null)type = service.getTypes().getTypesByName().get(method.getAnnotation(com.ethereal.server.Core.Annotation.AbstractType.class).abstractName());
                            if(type == null)return new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundAbstractType,String.format("RPC中的%s类型参数尚未被注册！",return_type),null),request.getId(),request.getService());
                            return new ClientResponseModel(type.getSerialize().Serialize(result),type.getName(),null,request.getId(),request.getService());
                        }
                        else return null;
                    }
                    else return new ClientResponseModel(null,null,new Error(Error.ErrorCode.Intercepted,"请求已被拦截",null),request.getId(),request.getService());
                }
                else return new ClientResponseModel(null,null,new Error(Error.ErrorCode.Intercepted, String.format("未找到方法%s-%s-%s",name,request.getService(),request.getMethodId() ),null),request.getId(),request.getService());
            }
            else return new ClientResponseModel(null,null,new Error(Error.ErrorCode.Intercepted, String.format("未找到服务%s-%s",name,request.getService()),null),request.getId(),request.getService());
        }
        catch (Exception e){
            return new ClientResponseModel(null,null,new Error(Error.ErrorCode.Intercepted, String.format("%s\n%s",e.getMessage(), Arrays.toString(e.getStackTrace())),null),request.getId(),request.getService());
        }
    }

    public ExceptionEvent getExceptionEvent() {
        return exceptionEvent;
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }
    @Override

    public void onException(TrackException.ErrorCode code, String message){
        onException(new TrackException(code,message));
    }
    @Override
    public void onException(TrackException exception)  {
        exception.setNet(this);
        exceptionEvent.onEvent(exception);
    }
    @Override

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message));
    }
    @Override
    public void onLog(TrackLog log){
        log.setNet(this);
        logEvent.onEvent(log);
    }
}
