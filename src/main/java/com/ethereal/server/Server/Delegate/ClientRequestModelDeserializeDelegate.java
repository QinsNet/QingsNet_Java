package com.ethereal.server.Server.Delegate;

import com.ethereal.server.Core.Model.ClientRequestModel;

public interface ClientRequestModelDeserializeDelegate {
    ClientRequestModel Deserialize(String obj);
}
