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
    @Getter
    protected Meta meta;
    protected Net(Meta meta){
        this.meta = meta;
    }

}
