package com.ethereal.meta.net.core;

import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.INetwork;
import com.ethereal.meta.service.core.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Net extends Service implements INet {

    protected NetConfig netConfig;
    @Setter
    protected INetwork INetwork;


    public void onConnectSuccess(){
        for (Meta meta : metas.values()){
            if(meta.getINetwork() == INetwork){
                meta.onConnectSuccess();
            }
        }
    }
    public void onConnectFail(){
        for (Meta meta : metas.values()){
            if(meta.getINetwork() == INetwork){
                meta.onConnectFail();
            }
        }
    }
    public void onConnectLost(){
        for (Meta meta : metas.values()){
            if(meta.getINetwork() == INetwork){
                meta.onConnectLost();
            }
        }
    }

}
