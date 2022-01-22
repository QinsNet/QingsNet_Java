package com.ethereal.net.service.annotation;

import com.ethereal.net.core.entity.TrackException;

public interface IService{
    void initialize() throws TrackException;
    void register();
    void unregister();
    void unInitialize();
}
