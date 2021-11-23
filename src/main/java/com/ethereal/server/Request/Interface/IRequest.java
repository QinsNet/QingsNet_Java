package com.ethereal.server.Request.Interface;

import com.ethereal.server.Core.Interface.IExceptionEvent;
import com.ethereal.server.Core.Interface.ILogEvent;
import com.ethereal.server.Core.Model.TrackException;

public interface IRequest extends IExceptionEvent, ILogEvent {
    void initialize() throws TrackException;
    void register();
    void unregister();
    void unInitialize();
}
