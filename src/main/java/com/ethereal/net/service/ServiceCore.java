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


}
