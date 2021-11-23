package com.ethereal.server.Server.EventRegister.Delegate;

import com.ethereal.server.Service.Abstract.Token;

public interface ConnectDelegate {
    void onConnect(Token token);
}
