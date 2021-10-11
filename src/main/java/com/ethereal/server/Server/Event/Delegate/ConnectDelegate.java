package com.ethereal.server.Server.Event.Delegate;

import com.ethereal.server.Server.Abstract.Token;

public interface ConnectDelegate {
    void onConnect(Token token);
}
