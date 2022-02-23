package com.qins.net.node.core;

import com.qins.net.core.entity.NodeAddress;
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
