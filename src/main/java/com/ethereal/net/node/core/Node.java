package com.ethereal.net.node.core;

import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.node.network.INetwork;
import com.ethereal.net.node.event.ConnectEvent;
import com.ethereal.net.node.event.DisConnectEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Node extends BaseCore implements INode {

    protected ConnectEvent connectEvent = new ConnectEvent();
    protected DisConnectEvent disConnectEvent = new DisConnectEvent();
    @Setter
    protected INetwork network;

    public void onConnect(){
        connectEvent.onEvent(this);
    }

    public void onDisConnect(){
        disConnectEvent.onEvent(this);
    }
}
