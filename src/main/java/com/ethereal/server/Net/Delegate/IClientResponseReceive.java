package com.ethereal.server.Net.Delegate;

import com.ethereal.server.Core.Model.ClientResponseModel;
import com.ethereal.server.Core.Model.TrackException;

public interface IClientResponseReceive {
    public void ClientResponseReceive(ClientResponseModel request) throws TrackException;
}
