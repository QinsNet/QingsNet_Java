package Server.Interface;

import Core.Interface.IExceptionEvent;
import Core.Interface.ILogEvent;

public interface IServer extends ILogEvent, IExceptionEvent {
    void Start();
    void Close();
}
