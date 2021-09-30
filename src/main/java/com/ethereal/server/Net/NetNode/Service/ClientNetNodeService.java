package com.ethereal.server.Net.NetNode.Service;

import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Service.WebSocket.WebSocketService;

public class ClientNetNodeService extends WebSocketService {

    public ClientNetNodeService(String name, AbstractTypes types) {
        super(name, types);
    }
}
