package com.qins.net.node.core;

import com.qins.net.meta.core.MetaClass;
import com.qins.net.request.core.RequestContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Node implements INode {
    protected NodeConfig nodeConfig = new NodeConfig();
    protected MetaClass metaClass;
    protected RequestContext context;
    public Node(){

    }
}
