package com.ethereal.server.Service.Interface;

import com.ethereal.server.Core.Interface.IExceptionEvent;
import com.ethereal.server.Core.Interface.ILogEvent;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Service.Abstract.ServiceConfig;

public interface IService{
    void initialize() throws TrackException;
    void register();
    void unregister();
    void unInitialize();
}
