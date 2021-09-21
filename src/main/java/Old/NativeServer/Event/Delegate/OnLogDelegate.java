package Old.NativeServer.Event.Delegate;

import Model.RPCLog;
import NativeServer.ServerListener;

public interface OnLogDelegate {
    void OnLog(RPCLog log, ServerListener client);
}
