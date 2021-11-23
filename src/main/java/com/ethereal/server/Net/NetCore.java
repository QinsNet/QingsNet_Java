package com.ethereal.server.Net;

import com.ethereal.server.Core.Enums.NetType;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.Abstract.NetConfig;
import com.ethereal.server.Net.WebSocket.WebSocketNet;
import com.ethereal.server.Net.WebSocket.WebSocketNetConfig;
import com.ethereal.server.Request.Abstract.Request;

import java.util.HashMap;

public class NetCore {

    private final static HashMap<String, Net> nets = new HashMap<>();

    public static Net get(String name)
    {
        return nets.get(name);
    }

    public static Net register(Net net) throws TrackException {
        if (!nets.containsKey(net.getName()))
        {
            nets.put(net.getName(), net);
            return net;
        }
        else throw new TrackException(TrackException.ErrorCode.Core,String.format("%s已注册,无法重复注册！", net.getName()));
    }

    public static Boolean unregister(String name)  {
        Net net = get(name);
        return unregister(net);
    }
    public static Boolean unregister(Net net)
    {
        if(net != null){
            if(nets.containsKey(net.getName())){
                nets.remove(net.getName());
            }
        }
        return true;
    }

}
