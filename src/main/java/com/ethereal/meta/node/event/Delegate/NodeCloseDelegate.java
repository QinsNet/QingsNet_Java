package com.ethereal.meta.node.event.Delegate;

import com.ethereal.meta.node.core.Node;

public interface NodeCloseDelegate {
    void onDisConnect(Node node);
}
