package com.ethereal.server.Service.WebSocket;
import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Service.Abstract.Service;

public abstract class WebSocketService extends Service {
    public WebSocketService(){
        config = new WebSocketServiceConfig();
    }
}
