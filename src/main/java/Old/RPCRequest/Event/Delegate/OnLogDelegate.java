package Old.RPCRequest.Event.Delegate;

import Model.RPCLog;
import RPCRequest.Request;

public interface OnLogDelegate {
    void OnLog(RPCLog log, Request request);
}
