package com.ethereal.meta.net.core;

import com.ethereal.meta.net.network.Network;
import com.ethereal.meta.service.core.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Net extends Service implements INet {

    protected NetConfig netConfig;
    @Setter
    protected Network network;


    public void onConnectSuccess(){

    }
    public void onConnectFail(){

    }
    public void onConnectLost(){

    }

}
