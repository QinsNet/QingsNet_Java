package com.ethereal.server.Net.WebSocket;


import com.ethereal.server.Core.Enums.NetType;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Core.Model.TrackLog;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.NetCore;
import com.ethereal.server.Request.Abstract.Request;
import com.ethereal.server.Request.RequestCore;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Service.ServiceCore;
import com.ethereal.server.Utils.AutoResetEvent;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class WebSocketNet extends Net {
    AutoResetEvent netNodeSign = new AutoResetEvent(false);
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
        try {
            new Thread(()->server.Start()).start();
        } catch (Exception e){
            onException(new TrackException(e));
        }
        return true;
    }
}
