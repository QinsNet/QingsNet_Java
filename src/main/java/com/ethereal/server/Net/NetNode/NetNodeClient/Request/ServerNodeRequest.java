package com.ethereal.server.Net.NetNode.NetNodeClient.Request;

import com.ethereal.client.Core.Model.AbstractTypes;
import com.ethereal.client.Core.Model.TrackException;
import com.ethereal.client.Request.Annotation.Request;
import com.ethereal.client.Request.WebSocket.WebSocketRequest;
import com.ethereal.server.Net.NetNode.Model.NetNode;

public class ServerNodeRequest extends WebSocketRequest {
    public ServerNodeRequest() throws TrackException {
        name="ServerNetNodeService";
        //注册数据类型
        types.add(Integer.class,"Int");
        types.add(Long.class,"Long");
        types.add(String.class,"String");
        types.add(Boolean.class,"Bool");
        types.add(NetNode.class,"NetNode");
    }
    @Request
    public Boolean Register(NetNode node){
        return Boolean.TRUE;
    }
}
