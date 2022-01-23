package com.ethereal.net.node.event.Delegate;

import com.ethereal.net.node.core.Node;

public interface NodeCloseDelegate {
    void onDisConnect(Node node);
}
