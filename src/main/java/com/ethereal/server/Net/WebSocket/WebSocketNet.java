package com.ethereal.server.Net.WebSocket;

import com.ethereal.server.Core.Enums.NetType;
import com.ethereal.server.Net.Abstract.Net;

import java.util.concurrent.Semaphore;

public class WebSocketNet extends Net {
    private Semaphore connectSign = new Semaphore(0);

    public WebSocketNetConfig getConfig() {
        return (WebSocketNetConfig)config;
    }

    public WebSocketNet(String name){
        super(name);
        netType = NetType.WebSocket;
        config = new WebSocketNetConfig();
    }
    @Override
    public boolean publish() throws Exception {
        //分布式模式
        if(config.getNetNodeMode()){

        }
        else {
            new Thread(()->server.Start()).start();
        }
        return true;
    }
}
