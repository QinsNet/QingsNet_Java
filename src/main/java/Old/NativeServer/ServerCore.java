package Old.NativeServer;

import Model.RPCLog;
import NativeServer.Event.Delegate.OnExceptionDelegate;
import NativeServer.Event.Delegate.OnLogDelegate;
import NativeServer.Interface.CreateInstanceDelegate;
import NativeServer.ServerConfig;
import NativeServer.ServerListener;
import RPCNet.Net;
import RPCNet.NetCore;
import RPCRequest.Request;
import RPCRequest.RequestCore;
import org.javatuples.Pair;
import org.javatuples.Tuple;

public class ServerCore {
    public static ServerListener get(String netName)
    {
        Net net = NetCore.get(netName);
        if (net != null)
        {
            return net.getServer();
        }
        else
        {
            return null;
        }
    }
    public static ServerListener get(Net net){
        Request request = RequestCore.getRequest(net,serviceName);
        if(request != null){
            return  request.getClient();
        }
        else return null;

    }

    public static ServerListener Register(Net net, String ip, String port, CreateInstanceDelegate createMethod)
    {
        return Register(net, ip, port, new ServerConfig(createMethod),null);
    }
    public static ServerListener Register(Net net, String ip, String port,ServerConfig config)
    {
        return Register(net, ip, port,config,null);
    }
    /// <summary>
    /// 获取客户端
    /// </summary>
    /// <param name="serverIp">远程服务IP</param>
    /// <param name="port">远程服务端口</param>
    /// <returns>客户端</returns>
    public static ServerListener Register(Net net, String ip, String port,ServerConfig config,ServerListener socketserver)
    {
        Pair<String, String> key = new Pair<String, String>(ip, port);
        if (net.getServer() == null)
        {
            if (socketserver == null) socketserver = new ServerListener(net, key, config);
            net.setServer(socketserver);
            net.getServer().getLogEvent().register(new OnLogDelegate() {
                @Override
                public void OnLog(RPCLog log, ServerListener client) {

                }
            });
            net.getServer().getExceptionEvent().register(new OnExceptionDelegate() {
                @Override
                public void OnException(Exception exception, ServerListener client) throws Exception {

                }
            });
        }
        return socketserver;
    }

    public static boolean UnRegister(String netName)
    {
        Net net = NetCore.get(netName);
        if (net!=null)
        {
            return UnRegister(net);
        }
        else
        {
            return true;
        }
    }
    public static boolean UnRegister(Net net)
    {
        net.Server.LogEvent -= net.OnServerLog;
        net.Server.ExceptionEvent -= net.OnServerException;
        net.Server.Stop();
        net.Server.Dispose();
        net.Server = null;
        net.ServerRequestSend = null;
        net.ClientResponseSend = null;
        return true;
    }
}
