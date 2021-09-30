package com.ethereal.server.Server.Delegate;

import com.ethereal.server.Core.Model.ServerRequestModel;

public interface ServerRequestModelSerializeDelegate {
    String Serialize(ServerRequestModel obj);
}

