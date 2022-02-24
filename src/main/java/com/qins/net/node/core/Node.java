package com.qins.net.node.core;

import com.qins.net.meta.core.MetaNodeField;
import com.qins.net.request.core.RequestContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Node implements INode {
    protected NodeConfig nodeConfig = new NodeConfig();
    protected MetaNodeField metaNodeField;
    protected RequestContext context;
    public Node(){

    }
}
