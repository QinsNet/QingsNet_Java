package com.ethereal.net.request.core;

import com.ethereal.net.core.entity.TrackException;

public interface IRequest{
    void initialize() throws TrackException;
    void register();
    void unregister();
    void unInitialize();
}
