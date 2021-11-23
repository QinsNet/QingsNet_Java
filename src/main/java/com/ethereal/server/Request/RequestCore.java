package com.ethereal.server.Request;

import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Request.Abstract.RequestInterceptor;
import com.ethereal.server.Request.Annotation.RequestMapping;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Service.ServiceCore;
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
        if(!service.getRequests().containsKey(request.getName())){
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
    public static boolean unregister(Request request)  {
        request.unregister();
        request.getService().getRequests().remove(request.getName());
        request.setService(null);
        request.unInitialize();
        return true;
    }
}
