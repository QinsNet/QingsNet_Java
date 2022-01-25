package com.ethereal.meta.net.core.event.Delegate;

import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.net.network.Network;

public interface ActiveDelegate {
    void onActive(Net net);
}
