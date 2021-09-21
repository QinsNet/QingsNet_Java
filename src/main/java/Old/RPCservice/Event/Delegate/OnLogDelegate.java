package Old.RPCservice.Event.Delegate;

import Model.RPCLog;
import RPCservice.Service;

public interface OnLogDelegate {
    void OnLog(RPCLog log, Service service);
}
