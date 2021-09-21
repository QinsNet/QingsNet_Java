package Old.RPCNet.Event.Delegate;

import RPCNet.Net;

public interface OnExceptionDelegate {
    void OnException(Exception exception, Net net);
}
