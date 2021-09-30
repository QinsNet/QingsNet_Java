package com.ethereal.server.Server;

import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.NetCore;
import com.ethereal.server.Server.Abstract.Server;

public class ServerCore {

    public static Server get(String netName)  {
        Net net = NetCore.get(netName);//获取对应的网络节点
        if(net != null){
            return net.getServer();
        }
        else return null;
    }

    public static Server register(Net net,Server server) throws TrackException {
        if(net.getServer() == null){
            net.setServer(server);
            server.setNetName(net.getName());
            server.getLogEvent().register(net::onLog);//日志系统
            server.getExceptionEvent().register(net::onException);//异常系统
        }
        return server;
    }

    public static boolean unregister(String netName)  {
        Net net = NetCore.get(netName);
        return unregister(net);
    }

    public static boolean unregister(Net net)  {
        if(net.getServer() != null){
            net.getServer().Close();
            return true;
        }
        else return true;
    }
}
