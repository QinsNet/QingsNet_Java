package com.ethereal.server.Net.NetNode.NetNodeClient.Service;

import com.ethereal.client.Core.Model.AbstractTypes;
import com.ethereal.client.Core.Model.TrackException;
import com.ethereal.client.Service.WebSocket.WebSocketService;
import com.ethereal.server.Net.NetNode.Model.NetNode;

public class ClientNodeService extends WebSocketService {
    public ClientNodeService() throws TrackException {
        name="ClientNetNodeService";
        //注册数据类型
        types.add(Integer.class,"Int");
        types.add(Long.class,"Long");
        types.add(String.class,"String");
        types.add(Boolean.class,"Bool");
        types.add(NetNode.class,"NetNode");
    }
}
