package Server.Interface;

import Core.Interface.IExceptionEvent;
import Core.Interface.ILogEvent;

public interface IBaseToken extends ILogEvent, IExceptionEvent {
    void DisConnect(String reason);
}
