package com.ethereal.server.Net.WebSocket;

import com.ethereal.client.Client.Abstract.Client;
import com.ethereal.client.Client.Abstract.ClientConfig;
import com.ethereal.client.Client.ClientCore;
import com.ethereal.client.Client.Event.Delegate.OnConnectFailDelegate;
import com.ethereal.client.Client.Event.Delegate.OnConnectSuccessDelegate;
import com.ethereal.client.Client.Event.Delegate.OnDisConnectDelegate;
import com.ethereal.client.Client.WebSocket.WebSocketClient;
import com.ethereal.client.Client.WebSocket.WebSocketClientConfig;
import com.ethereal.server.Core.Enums.NetType;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.NetCore;
import com.ethereal.server.Net.NetNode.Model.NetNode;
import com.ethereal.server.Net.NetNode.Model.ServiceNode;
import com.ethereal.server.Net.NetNode.NetNodeClient.Request.ServerNodeRequest;
import com.ethereal.server.Net.NetNode.NetNodeClient.Service.ClientNodeService;
import com.ethereal.server.Net.NetNode.NetNodeServer.Request.ClientNodeRequest;
import com.ethereal.server.Net.NetNode.NetNodeServer.Service.ServerNodeService;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Request.RequestCore;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Service.ServiceCore;
import com.ethereal.server.Utils.AutoResetEvent;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class WebSocketNet extends Net {
    AutoResetEvent netNodeSign = new AutoResetEvent(false);
    public WebSocketNetConfig getConfig() {
        return (WebSocketNetConfig)config;
    }

    public WebSocketNet(String name){
        super(name);
        netType = NetType.WebSocket;
        config = new WebSocketNetConfig();
    }
    @Override
    public boolean publish() throws Exception {
        try {
            //分布式模式
            if(config.getNetNodeMode()){
                //服务端
                ServiceCore.register(this,new ServerNodeService());
                RequestCore.register(this,ClientNodeRequest.class);
                //客户端
                new Thread(()->{
                    while (NetCore.get(name)!=null){
                        try {
                            for (Pair<String ,ClientConfig> pair : config.getNetNodeIps()){
                                String prefixes = pair.getValue0();
                                ClientConfig clientConfig = pair.getValue1();
                                com.ethereal.client.Net.Abstract.Net net = com.ethereal.client.Net.NetCore.get(String.format("NetNodeClient-%s", prefixes));
                                if(net == null){
                                    net = com.ethereal.client.Net.NetCore.register(new com.ethereal.client.Net.WebSocket.WebSocketNet(String.format("NetNodeClient-%s", prefixes)));
                                    net.getConfig().setNetNodeMode(false);
                                    net.getLogEvent().register((log)->onLog(TrackLog.LogCode.Runtime,"来自NetNode搜寻节点客户端的日志:\n" + log.getMessage()));
                                    net.getExceptionEvent().register((exception)->onException(TrackException.ErrorCode.Runtime,"来自NetNode搜寻节点客户端的异常:\n" + exception.getException().getMessage()));
                                }
                                com.ethereal.client.Request.Abstract.Request serverNodeRequest = com.ethereal.client.Request.RequestCore.get(net,"ServerNetNodeService");
                                if(serverNodeRequest == null){
                                    serverNodeRequest = com.ethereal.client.Request.RequestCore.register(net,ServerNodeRequest.class);
                                }
                                com.ethereal.client.Service.Abstract.Service clientNodeService = com.ethereal.client.Service.ServiceCore.get(net,"ClientNetNodeService");
                                if(clientNodeService == null){
                                    com.ethereal.client.Service.ServiceCore.register(net,new ClientNodeService());
                                }
                                Client client = serverNodeRequest.getClient();
                                if(client == null){
                                    client = new WebSocketClient(prefixes);
                                    if(clientConfig!=null)client.setConfig(clientConfig);
                                    //注册连接
                                    ClientCore.register(serverNodeRequest,client);
                                    client.getConnectSuccessEvent().register(new OnConnectSuccessDelegate() {
                                        @Override
                                        public void OnConnectSuccess(Client client) {
                                            ServerNodeRequest serverNodeRequest = com.ethereal.client.Request.RequestCore.get(String.format("NetNodeClient-%s",client.getPrefixes()),"ServerNetNodeService");
                                            if(serverNodeRequest == null){
                                                client.onException(com.ethereal.client.Core.Model.TrackException.ErrorCode.Runtime,String.format("EtherealC中未找到 NetNodeClient-%s-ServerNodeService", client.getPrefixes()));
                                            }
                                            NetNode node = new NetNode();
                                            node.setPrefixes(server.getPrefixes().toArray(new String[0]));
                                            node.setName(name);
                                            node.setServices(new HashMap<>());
                                            node.setRequests(new HashMap<>());
                                            for(Service service : services.values()){
                                                ServiceNode serviceNode = new ServiceNode();
                                                serviceNode.setName(service.getName());
                                                node.getServices().put(serviceNode.getName(),serviceNode);
                                            }
                                            for(Request request : requests.values()){
                                                ServiceNode requestNode = new ServiceNode();
                                                requestNode.setName(request.getName());
                                                node.getServices().put(requestNode.getName(),requestNode);

                                            }
                                            serverNodeRequest.Register(node);
                                        }
                                    });
                                    client.getConnectFailEvent().register(new OnConnectFailDelegate() {
                                        @Override
                                        public void OnConnectFail(Client client) {
                                            ClientCore.unregister(client.getNetName(),client.getServiceName());
                                            onLog(TrackLog.LogCode.Runtime,String.format("NetNode-%s 连接失败，等待下一轮重连.", client.getPrefixes()));
                                        }
                                    });
                                    client.getDisConnectEvent().register(new OnDisConnectDelegate() {
                                        @Override
                                        public void OnDisConnect(Client client) {
                                            ClientCore.unregister(client.getNetName(),client.getServiceName());
                                        }
                                    });
                                    client.connect();
                                }
                            }
                        }
                        catch (Exception e){
                            onException(new TrackException(e));
                        }
                        finally {
                            try {
                                netNodeSign.waitOne(config.getNetNodeHeartInterval(), TimeUnit.MILLISECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
            new Thread(()->server.Start()).start();
        }
        catch (TrackException e){
            onException(e);
        }
        catch (Exception e){
            onException(new TrackException(e));
        }
        return true;
    }
}
