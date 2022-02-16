package com.ethereal.meta.node.core;

import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.request.core.RequestContext;
import lombok.Getter;

@Getter
public abstract class Node implements INode {
    protected NodeConfig nodeConfig = new NodeConfig();
    protected Meta meta;
    protected RequestContext context;
    public Node(Meta meta, RequestContext context){
        this.meta = meta;
        this.context = context;
    }
}
