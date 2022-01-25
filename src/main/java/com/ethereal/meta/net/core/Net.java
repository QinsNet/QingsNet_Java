package com.ethereal.meta.net.core;

import com.ethereal.meta.net.network.Network;
import com.ethereal.meta.net.core.event.ActiveEvent;
import com.ethereal.meta.net.core.event.InactiveEvent;
import com.ethereal.meta.service.core.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Net extends Service implements INet {

    protected NetConfig netConfig;
    @Setter
    protected Network network;

    public ActiveEvent activeEvent = new ActiveEvent();
    public InactiveEvent inactiveEvent = new InactiveEvent();


    public void onConnect(){

    }
    public void onDisconnect(){

    }

}
