package com.ethereal.net.net.core;

import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.base.event.ExceptionEvent;
import com.ethereal.net.core.base.event.LogEvent;
import com.ethereal.net.core.entity.Error;
import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.core.entity.ResponseMeta;
import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.core.entity.TrackLog;
import com.ethereal.net.node.core.Node;
import com.ethereal.net.request.core.Request;
import com.ethereal.net.service.core.Service;
import com.ethereal.net.service.event.delegate.InterceptorDelegate;
import com.ethereal.net.service.event.InterceptorEvent;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;


public class Net extends BaseCore implements INet {
    static HashMap<String,Net> nets = new HashMap<String,Net>();
    @Getter
    private NetConfig config;
    @Getter
    private String name;
    @Getter
    private HashMap<String, Service> services = new HashMap<>();
    @Getter
    private HashMap<String, Request> request = new HashMap<>();
    @Getter
    private InterceptorEvent interceptorEvent = new InterceptorEvent();
    @Getter
    private Node node;

    public boolean onInterceptor(RequestMeta request)
    {
        if (interceptorEvent != null)
        {
            for (InterceptorDelegate item : interceptorEvent.getListeners())
            {
                if (!item.onInterceptor(request)) return false;
            }
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

    public static <T> T register(Service service) throws TrackException {
        service.initialize();
        if(!service.getRegister()){
            service.setRegister(true);
            Service.register(service);
            service.getExceptionEvent().register(net::onException);
            service.getLogEvent().register(net::onLog);
            net.getServices().put(service.getName(),service);
            service.register();
            return (T) service;
        }
        else throw new TrackException(TrackException.ErrorCode.Core,String.format("%s-%s已注册,无法重复注册！",net.getName(),service.getName()));
    }
}
