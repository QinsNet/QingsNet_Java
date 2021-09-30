package com.ethereal.server.Net.NetNode.Request;

import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Request.Annotation.Request;
import com.ethereal.server.Net.NetNode.Model.NetNode;
import com.ethereal.server.Request.WebSocket.WebSocketRequest;

public class ServerNetNodeRequest extends WebSocketRequest {
    public ServerNetNodeRequest(String name, AbstractTypes types) {
        super(name, types);
    }

    @Request
    public NetNode GetNetNode(String servicename){
        return null;
    }
}
