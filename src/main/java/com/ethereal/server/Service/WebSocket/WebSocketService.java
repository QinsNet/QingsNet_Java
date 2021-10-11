package com.ethereal.server.Service.WebSocket;
import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Service.Abstract.Service;

public class WebSocketService extends Service {
    public WebSocketService(){
        config = new WebSocketServiceConfig();
    }
}
