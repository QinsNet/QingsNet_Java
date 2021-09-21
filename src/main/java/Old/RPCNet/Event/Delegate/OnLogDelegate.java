package Old.RPCNet.Event.Delegate;

import Model.RPCLog;
import RPCNet.Net;

public interface OnLogDelegate {
    void OnLog(RPCLog log, Net net);
}
