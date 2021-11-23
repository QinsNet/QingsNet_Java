package com.ethereal.server.Service.Interface;

import com.ethereal.server.Core.Interface.IExceptionEvent;
import com.ethereal.server.Core.Interface.ILogEvent;

public interface IToken extends ILogEvent, IExceptionEvent {
    void disConnect(String reason);
}
