package com.ethereal.server.Server.Event.Delegate;

import com.ethereal.server.Server.Abstract.BaseToken;

public interface ConnectDelegate {
    void onConnect(BaseToken token);
}
