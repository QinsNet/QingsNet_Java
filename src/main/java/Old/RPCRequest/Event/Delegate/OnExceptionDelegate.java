package Old.RPCRequest.Event.Delegate;

import RPCRequest.Request;

public interface OnExceptionDelegate {
    void OnException(Exception exception, Request request);
}
