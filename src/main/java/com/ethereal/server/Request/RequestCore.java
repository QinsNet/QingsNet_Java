package com.ethereal.server.Request;

import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.NetCore;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Server.ServerCore;
import com.ethereal.server.Service.ServiceCore;

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

    public static <T> T register(Net net,Class<?> requestClass, String serviceName, AbstractTypes types) throws TrackException {
        Request request = null;
        if(!net.getRequests().containsKey(serviceName)){
            try{
                request = Request.register((Class<Request>) requestClass,serviceName, types);
                request.setNetName(net.getName());
                request.getExceptionEvent().register(net::onException);
                request.getLogEvent().register(net::onLog);
                net.getRequests().put(serviceName, request);
            }
            catch (Exception err){
                throw new TrackException(TrackException.ErrorCode.Core,serviceName + "异常报错，销毁注册\n" + err.getMessage());
            }
        }
        else throw new TrackException(TrackException.ErrorCode.Core,String.format("%s-%s已注册,无法重复注册！", net.getName(),serviceName));
        return (T)request;
    }

    public static boolean unregister(String netName,String serviceName)  {
        Net net = NetCore.get(netName);
        if (net == null)
            return true;
        return unregister(net, serviceName);
    }
    public static boolean unregister(Net net,String serviceName)  {
        Request request = get(net,serviceName);
        if(request != null){
            net.getRequests().remove(serviceName);
        }
        return true;
    }

}
