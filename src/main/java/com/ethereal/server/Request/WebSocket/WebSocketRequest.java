package com.ethereal.server.Request.WebSocket;

import com.ethereal.server.Core.Model.AbstractType;
import com.ethereal.server.Core.Model.AbstractTypes;
import com.ethereal.server.Request.Abstract.Request;

public class WebSocketRequest extends Request {
    public WebSocketRequest(String name, AbstractTypes types){
        super(name,types);
        config = new WebSocketRequestConfig();
    }
}
