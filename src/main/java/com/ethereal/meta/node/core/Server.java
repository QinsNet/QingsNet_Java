package com.ethereal.meta.node.core;

import com.ethereal.meta.core.entity.NodeAddress;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Server {
    @Getter
    protected NodeAddress local;
    public abstract boolean start();
    public abstract boolean close();
}
