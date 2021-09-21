package Old.NativeServer.Event.Delegate;

import NativeServer.ServerListener;

public interface OnConnectFailDelegate {
    void OnConnectFail(ServerListener client) throws Exception;
}
