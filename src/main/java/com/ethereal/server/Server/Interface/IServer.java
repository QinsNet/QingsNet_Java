package com.ethereal.server.Server.Interface;

import com.ethereal.server.Core.Interface.IExceptionEvent;
import com.ethereal.server.Core.Interface.ILogEvent;

public interface IServer extends ILogEvent, IExceptionEvent {
    void Start();
    void Close();
}
