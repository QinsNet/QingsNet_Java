package com.ethereal.server.Net.NetNode.NetNodeServer.Request;

import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.NetNode.Model.NetNode;
import com.ethereal.server.Request.WebSocket.WebSocketRequest;

public class ClientNodeRequest extends WebSocketRequest {
    public ClientNodeRequest() throws TrackException {
        name = "ClientNetNodeService";
        types = new AbstractTypes();
        //注册数据类型
        types.add(Integer.class,"Int");
        types.add(Long.class,"Long");
        types.add(String.class,"String");
        types.add(Boolean.class,"Bool");
        types.add(NetNode.class,"NetNode");
    }
}
