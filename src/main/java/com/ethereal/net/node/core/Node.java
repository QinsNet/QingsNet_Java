package com.ethereal.net.node.core;

import com.ethereal.net.node.event.ConnectEvent;
import com.ethereal.net.node.event.DisConnectEvent;
import com.ethereal.net.node.network.INetwork;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Node implements INode {
    @Setter
    protected INetwork network;
    protected ConnectEvent connectEvent = new ConnectEvent();
    protected DisConnectEvent disConnectEvent = new DisConnectEvent();

    public void onConnect(){
        connectEvent.onEvent(this);
    }

    public void onDisConnect(){
        disConnectEvent.onEvent(this);
    }

}
