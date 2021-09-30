package com.ethereal.server.Net.Delegate;

import com.ethereal.server.Core.Model.ServerRequestModel;

public interface IServerRequestReceive {
    public void ServerRequestReceive(ServerRequestModel request) throws Exception;
}
