package com.ethereal.meta.node.core;

import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.request.core.RequestContext;
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
