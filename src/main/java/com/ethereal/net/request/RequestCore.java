package com.ethereal.net.request;

import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.request.core.Request;
import com.ethereal.net.request.core.RequestInterceptor;
import com.ethereal.net.request.annotation.RequestMapping;
import com.ethereal.net.service.core.Service;
import com.ethereal.net.service.ServiceCore;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class RequestCore {
    //获取Request实体
    public static <T> T get(String netName,String serviceName,String requestName)  {
        Service service = ServiceCore.get(netName,serviceName);
        if(service == null){
            return null;
        }
        return get(service,requestName);
    }
    //获取Request实体
    public static <T> T get(Service service,String requestName)  {
        Object request = service.getRequests().get(requestName);
        return (T)request;
    }

    public static <T> T register(Service service, Class<?> requestClass) throws TrackException {
        return register(service,requestClass,null);
    }
    public static <T> T register(Service service, Class<?> requestClass, String serviceName) throws TrackException {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(requestClass);
        RequestInterceptor interceptor = new RequestInterceptor();
        Callback noOp= NoOp.INSTANCE;
        enhancer.setCallbacks(new Callback[]{noOp,interceptor});
        enhancer.setCallbackFilter(method -> {
            if(method.getAnnotation(RequestMapping.class) != null){
                return 1;
            }
            else return 0;
        });
        Request request = (Request)enhancer.create();
        request.initialize();
        if(serviceName!=null)request.setName(serviceName);
        if(!service.getRegister()){
            service.setRegister(true);
            Request.register(request);
            request.setService(service);
            request.getExceptionEvent().register(service::onException);
            request.getLogEvent().register(service::onLog);
            service.getRequests().put(request.getName(), request);
            request.register();
            return (T)request;
        }
        else throw new TrackException(TrackException.ErrorCode.Core,String.format("%s-%s已注册,无法重复注册！", service.getName(),serviceName));
    }
    public static boolean unregister(Request request) throws TrackException {
        if(request.getRegister()){
            request.unregister();
            request.getService().getRequests().remove(request.getName());
            request.setService(null);
            request.unInitialize();
            request.setRegister(false);
            return true;
        }
        else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s已经UnRegister,无法重复UnRegister", request.getName()));
    }
}
