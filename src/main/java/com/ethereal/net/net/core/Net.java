package com.ethereal.net.net.core;

import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.entity.Error;
import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.core.entity.ResponseMeta;
import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.node.core.Node;
import com.ethereal.net.request.core.Request;
import com.ethereal.net.service.core.Service;
import com.ethereal.net.service.event.InterceptorEvent;
import com.ethereal.net.service.event.delegate.InterceptorDelegate;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;


public class Net extends BaseCore implements INet {
    private static final HashMap<String,Net> nets = new HashMap<>();
    @Getter
    private NetConfig config;
    @Getter
    private final HashMap<String, Service> services = new HashMap<>();
    @Getter
    private final HashMap<String, Request> requests = new HashMap<>();
    @Getter
    private final InterceptorEvent interceptorEvent = new InterceptorEvent();
    @Getter
    private Node node;
    @Getter
    protected String name;
    private Net(){

    }

    public boolean onInterceptor(RequestMeta request)
    {
        for (InterceptorDelegate item : interceptorEvent.getListeners())
        {
            if (!item.onInterceptor(request)) return false;
        }
        return true;
    }

    public Object receiveProcess(RequestMeta requestMeta){
        try{
            //拦截器
            if(!onInterceptor(requestMeta)){
                return new ResponseMeta(requestMeta.getId(),new Error(Error.ErrorCode.Intercepted,"请求已被拦截"));
            }
            return requestMeta.getService().receiveProcess(requestMeta);
        }
        catch (Exception e){
            return new ResponseMeta(null, requestMeta.getId(),new com.ethereal.net.core.entity.Error(Error.ErrorCode.Exception, String.format("%s\n%s",e.getMessage(), Arrays.toString(e.getStackTrace()))));
        }
    }
    public static Net register(String net_name) throws TrackException {
        if(nets.containsKey(net_name)){
            throw new TrackException(TrackException.ErrorCode.Initialize, String.format("%s Net已存在", net_name));
        }
        Net net = new Net();
        net.name = net_name;
        nets.put(net.name,net);
        return net;
    }
    public boolean unRegister() throws TrackException {
        nets.remove(name);
        for(Service service : services.values()){
            service.unRegister();
        }
        for (Request request : requests.values()){
            request.unRegister();
        }
        return true;
    }
    public <T> T register(Service service) throws TrackException {
        if(!service.getInitialized()){
            service.setInitialized(true);
            service.initialize();
            service.setPrefixes(service.getName());
            service.setNet(this);
            service.getExceptionEvent().register(this::onException);
            service.getLogEvent().register(this::onLog);
            services.put(service.getName(),service);
            return (T) service;
        }
        else throw new TrackException(TrackException.ErrorCode.Initialize,String.format("%s/%s已注册,无法重复注册！",name,service.getName()));
    }
    public <T> T register(Request request) throws TrackException {
        if(!request.getInitialized()){
            request.setInitialized(true);
            request.initialize();
            request.setPrefixes(request.getName());
            request.setNet(this);
            request.getExceptionEvent().register(this::onException);
            request.getLogEvent().register(this::onLog);
            requests.put(request.getName(),request);
            return (T) request;
        }
        else throw new TrackException(TrackException.ErrorCode.Initialize,String.format("%s-%s已注册,无法重复注册！",name,request.getName()));
    }
}
