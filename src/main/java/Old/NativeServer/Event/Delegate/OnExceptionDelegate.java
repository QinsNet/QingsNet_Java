package Old.NativeServer.Event.Delegate;
import NativeServer.ServerListener;

public interface OnExceptionDelegate {
    void OnException(Exception exception, ServerListener client) throws Exception;
}
