package com.ethereal.server.Net;

import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.Abstract.Net;

import java.util.HashMap;

public class NetCore {

    private final static HashMap<String, Net> nets = new HashMap<>();

    public static Net get(String name)
    {
        return nets.get(name);
    }

    public static Net register(Net net) throws TrackException {
        if (!net.isRegister())
        {
            net.setRegister(true);
            nets.put(net.getName(), net);
            return net;
        }
        else throw new TrackException(TrackException.ErrorCode.Core,String.format("%s已注册,无法重复注册！", net.getName()));
    }

    public static Boolean unregister(Net net) throws TrackException {
        if(net.isRegister()){
            nets.remove(net.getName());
            net.setRegister(false);
            return true;
        }
        else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s已经UnRegister,无法重复UnRegister", net.getName()));
    }
}
