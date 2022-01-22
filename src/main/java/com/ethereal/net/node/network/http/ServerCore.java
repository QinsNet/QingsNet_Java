package com.ethereal.net.node.network.http;

import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.net.core.Net;
import com.ethereal.net.node.network.http.server.core.Server;

public class ServerCore {

    public static Server get(String netName)  {
        Net net = NetCore.get(netName);//获取对应的网络节点
        if(net != null){
            return net.getServer();
        }
        else return null;
    }

    public static Server register(Net net,Server server) throws TrackException {
        if(!server.getRegister()){
            server.setRegister(true);
            net.setServer(server);
            server.setNet(net);
            server.getLogEvent().register(net::onLog);//日志系统
            server.getExceptionEvent().register(net::onException);//异常系统
            return server;
        }
        else throw new TrackException(TrackException.ErrorCode.Core,String.format("%s-%s已注册,无法重复注册！", net.getName(),server.getPrefixes()));
    }

    public static boolean unregister(Server server) throws TrackException {
        if(server.getRegister()){
            server.getNet().setServer(null);
            server.setNet(null);
            server.close();
            server.setRegister(false);
            return true;
        }
        else throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s已经UnRegister,无法重复UnRegister", server.getPrefixes()));
    }
}
