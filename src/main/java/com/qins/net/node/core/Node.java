package com.qins.net.node.core;

import com.qins.net.meta.Meta;
import com.qins.net.request.core.RequestContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Node implements INode {
    protected NodeConfig nodeConfig = new NodeConfig();
    protected Meta meta;
    protected RequestContext context;
    public Node(){

    }
}
