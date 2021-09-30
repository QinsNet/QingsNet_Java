package com.ethereal.server.Net.Abstract;

import com.ethereal.server.Core.Enums.NetType;
import com.ethereal.server.Core.Event.ExceptionEvent;
import com.ethereal.server.Core.Event.LogEvent;
import com.ethereal.server.Core.Model.*;
import com.ethereal.server.Core.Model.Error;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.Delegate.IClientResponseReceive;
import com.ethereal.server.Net.Delegate.IServerRequestReceive;
import com.ethereal.server.Net.Interface.INet;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Server.Abstract.BaseToken;
import com.ethereal.server.Server.Abstract.Server;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Service.Event.Delegate.InterceptorDelegate;
import com.ethereal.server.Service.Event.InterceptorEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
    protected HashMap<Object, BaseToken> tokens = new HashMap<>();
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

    public HashMap<Object, BaseToken> getTokens() {
        return tokens;
    }

    public void setTokens(HashMap<Object, BaseToken> tokens) {
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

    public boolean OnInterceptor(Service service, Method method, BaseToken token)
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
    public ClientResponseModel clientRequestReceiveProcess(BaseToken token,ClientRequestModel request) {
        try {
            Method method;
            Service service = services.get(request.getService());
            if(service != null){
                method = service.getMethods().get(request.getMethodId());
                if(method!= null){
                    if(OnInterceptor(service,method,token) && service.OnInterceptor(this,method,token)){
                        //开始序列化参数
                        String[] param_id = request.getMethodId().split("-");
                        for (int i = 1; i < param_id.length; i++)
                        {
                            AbstractType rpcType = service.getTypes().getTypesByName().get(param_id[i]);
                            if(rpcType == null){
                                throw new TrackException(TrackException.ErrorCode.Runtime,String.format("RPC中的%s类型参数尚未被注册！",param_id[i]));
                            }
                            else request.getParams()[i] = rpcType.getDeserialize().Deserialize((String)request.getParams()[i]);
                        }
                        if(method.getParameterTypes().length == request.getParams().length)request.getParams()[0] = token;
                        else if(request.getParams().length > 1){
                            Object[] new_params = new Object[request.getParams().length - 1];
                            for(int i=0;i< new_params.length;i++){
                                new_params[i] = request.getParams()[i+1];
                            }
                            request.setParams(new_params);
                        }
                        Object result = method.invoke(service,request.getParams());
                        Class<?> return_type = method.getReturnType();
                        if(return_type != Void.class){
                            AbstractType type = service.getTypes().getTypesByType().get(return_type);
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
            return new ClientResponseModel(null,null,new Error(Error.ErrorCode.Intercepted, String.format("%s\n",e.getMessage()),null),request.getId(),request.getService());
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
