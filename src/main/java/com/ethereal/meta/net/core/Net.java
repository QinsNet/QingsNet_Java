package com.ethereal.meta.net.core;

import com.ethereal.meta.meta.Meta;
import lombok.Getter;

@Getter
public abstract class Net implements INet {
    protected NetConfig netConfig = new NetConfig();
    protected Meta meta;
    public Net(Meta meta){
        this.meta = meta;
    }
}
