package com.ethereal.server.Net.NetNode.NetNodeServer.Service;

import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Net.NetNode.Model.NetNode;
import com.ethereal.server.Server.Abstract.BaseToken;
import com.ethereal.server.Server.Event.Delegate.DisConnectDelegate;
import com.ethereal.server.Service.Annotation.Service;
import com.ethereal.server.Service.WebSocket.WebSocketService;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ServerNodeService extends WebSocketService {
    private HashMap<Object, Pair<BaseToken,NetNode>> netNodes = new HashMap<>();
    private Random random = new Random();
    public ServerNodeService() throws TrackException {
        name = "ServerNetNodeService";
        //注册数据类型
        types.add(Integer.class,"Int");
        types.add(Long.class,"Long");
        types.add(String.class,"String");
        types.add(Boolean.class,"Bool");
        types.add(NetNode.class,"NetNode");
    }

    @Service
    public Boolean Register(BaseToken token, NetNode netNode){
        token.key = (String.format("%s-%s", netNode.getName(),String.join("::",netNode.getPrefixes())));
        Pair<BaseToken,NetNode> pair = netNodes.get(token.key);
        if(pair != null){
            pair.getValue0().getDisConnectEvent().getListeners().clear();
            netNodes.remove(token.key);
        }
        netNodes.put(token.key,new Pair<>(token,netNode));
        token.getDisConnectEvent().register(new DisConnectDelegate() {
            @Override
            public void onDisConnect(BaseToken token) {
                netNodes.remove(token.key);
                onLog(TrackLog.LogCode.Runtime,String.format("节点已断开:{%s}", token.key));
                PrintNetNodes();
            }
        });
        onLog(TrackLog.LogCode.Runtime,String.format("节点注册成功:{%s}", token.key));
        PrintNetNodes();
        return true;
    }
    @Service
    public NetNode GetNetNode(BaseToken sender, String serviceName){
        ArrayList<NetNode> nodes = new ArrayList<>();
        for(Pair<BaseToken,NetNode> pair:netNodes.values()){
            if(pair.getValue1().getServices().containsKey(serviceName)){
                nodes.add(pair.getValue1());
            }
        }
        if(nodes.size() > 0){
            return nodes.get((random.nextInt() % nodes.size() + nodes.size()) % nodes.size());
        }
        return null;
    }

    public void PrintNetNodes(){
        StringBuilder sb = new StringBuilder();
        for(Pair<BaseToken,NetNode> pair:netNodes.values()){
            sb.append(String.join("&&",pair.getValue1().getPrefixes())).append("\n");
        }
        onLog(TrackLog.LogCode.Runtime,sb.toString());
    }

}
