package com.ethereal.server.Server.Delegate;

import com.ethereal.server.Core.Model.ClientResponseModel;

public interface ClientResponseModelSerializeDelegate {
    String Serialize(ClientResponseModel obj);
}
