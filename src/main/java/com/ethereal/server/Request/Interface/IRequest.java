package com.ethereal.server.Request.Interface;

import com.ethereal.server.Core.Interface.IExceptionEvent;
import com.ethereal.server.Core.Interface.ILogEvent;

public interface IRequest extends IExceptionEvent, ILogEvent {
    void initialize();
    void unInitialize();
}
