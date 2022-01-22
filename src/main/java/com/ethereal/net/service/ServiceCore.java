package com.ethereal.net.service;

import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.net.core.Net;
import com.ethereal.net.service.core.Service;

public class ServiceCore {

    public static <T> T get(String netName, String serviceName)  {
        Net net = NetCore.get(netName);
        if(net == null)return null;
        return get(net,serviceName);
    }

    public static <T> T register(Net net, Service service) throws TrackException {
        service.initialize();
        if(!service.getRegister()){
            service.setRegister(true);
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

    public static boolean unregister(Service service) throws TrackException {
        if(service.getRegister()){
            service.unregister();
            service.getNet().getServices().remove(service.getName());
            service.setNet(null);
            service.unInitialize();
            service.setRegister(false);
            return true;
        }
        else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s已经UnRegister,无法重复UnRegister", service.getName()));
    }
}
