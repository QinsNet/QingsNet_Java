package com.ethereal.server.Request.WebSocket;

import com.ethereal.server.Request.Abstract.Request;

public abstract class WebSocketRequest extends Request {
    public WebSocketRequest(){
        config = new WebSocketRequestConfig();
    }
}
