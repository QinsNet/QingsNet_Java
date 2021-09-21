package Old.RPCservice.Event.Delegate;

import RPCservice.Service;

public interface OnExceptionDelegate {
    void OnException(Exception exception, Service service);
}
