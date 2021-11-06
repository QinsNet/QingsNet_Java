package com.ethereal.server.Request;

import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.NetCore;
import com.ethereal.server.Request.Abstract.Request;

public class RequestCore {
    //获取Request实体
    public static <T> T get(String netName,String serviceName)  {
        Net net = NetCore.get(netName);
        if (net == null){
            return null;
        }
        else return (T)net.getRequests().get(serviceName);
    }
    //获取Request实体
    public static <T> T get(Net net,String serviceName)  {
        Object request = net.getRequests().get(serviceName);
        return (T)request;
    }

    public static <T> T register(Net net, Class<?> requestClass) throws TrackException {
        return register(net,requestClass,null,null);
    }
    public static <T> T register(Net net, Class<?> requestClass, String serviceName, AbstractTypes types) throws TrackException {
        Request request = Request.register((Class<Request>) requestClass);
        if(serviceName!=null)request.setName(serviceName);
        if(types!=null)request.setTypes(types);
        if(!net.getRequests().containsKey(request.getName())){
            request.setNet(net);
            request.getExceptionEvent().register(net::onException);
            request.getLogEvent().register(net::onLog);
            net.getRequests().put(request.getName(), request);
            return (T)request;
        }
        else throw new TrackException(TrackException.ErrorCode.Core,String.format("%s-%s已注册,无法重复注册！", net.getName(),serviceName));
    }
    public static boolean unregister(Request request)  {
        request.getNet().getServices().remove(request.getName());
        request.setNet(null);
        return true;
    }

}
