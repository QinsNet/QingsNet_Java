package com.ethereal.server.Service;

import com.ethereal.server.Core.Manager.AbstractType.AbstractTypeManager;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.NetCore;
import com.ethereal.server.Service.Abstract.Service;

public class ServiceCore {

    public static <T> T get(String netName, String serviceName)  {
        Net net = NetCore.get(netName);
        if(net == null)return null;
        return get(net,serviceName);
    }
    public static <T> T get(Net net,String serviceName)  {
        return (T)net.getServices().get(serviceName);
    }
    public static <T> T register(Net net,Service service) throws TrackException{
        return register(net,service,null,null);
    }
    public static <T> T register(Net net, Service service, String serviceName, AbstractTypeManager types) throws TrackException {
        service.initialize();
        if(serviceName!=null)service.setName(serviceName);
        if(types!=null)service.setTypes(types);
        if(!net.getServices().containsKey(service.getName())){
            Service.register(service);
            service.setNet(net);
            service.getExceptionEvent().register(net::onException);
            service.getLogEvent().register(net::onLog);
            net.getServices().put(service.getName(),service);
            service.register();
            return (T) service;
        }
        else throw new TrackException(TrackException.ErrorCode.Core,String.format("%s-%s已注册,无法重复注册！",net.getName(),service.getName()));
    }

    public static boolean unregister(Service service) {
        service.unregister();
        service.getNet().getServices().remove(service.getName());
        service.setNet(null);
        service.unInitialize();
        return true;
    }
}
