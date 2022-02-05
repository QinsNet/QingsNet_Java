package com.ethereal.meta.net.core;

import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.INetwork;
import com.ethereal.meta.request.core.Request;
import com.ethereal.meta.service.core.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Net implements INet {
    protected NetConfig netConfig;
    @Setter
    protected INetwork network;
    @Getter
    protected Meta meta;
    protected Net(Meta meta){
        this.meta = meta;
    }

    public void updateNetwork(INetwork parent) {
        if(!netConfig.isIndependence()){
            network = parent;
        }
    }

    public void onConnectSuccess(){
        for (Meta meta : meta.getMetas().values()){
            if(meta.getNet().getNetwork() == network){
                meta.getNet().onConnectSuccess();
            }
        }
    }
    public void onConnectFail(){
        for (Meta meta : meta.getMetas().values()){
            if(meta.getNet().getNetwork() == network){
                meta.getNet().onConnectFail();
            }
        }
    }
    public void onConnectLost(){
        for (Meta meta : meta.getMetas().values()){
            if(meta.getNet().getNetwork() == network){
                meta.getNet().onConnectLost();
            }
        }
    }
}
