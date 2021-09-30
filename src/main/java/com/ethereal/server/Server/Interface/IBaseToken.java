package com.ethereal.server.Server.Interface;

import com.ethereal.server.Core.Interface.IExceptionEvent;
import com.ethereal.server.Core.Interface.ILogEvent;

public interface IBaseToken extends ILogEvent, IExceptionEvent {
    void disConnect(String reason);

}
