package Old.RPCNet.Interface;

import Model.ServerRequestModel;

public interface IServerRequestSend {
    public void serverRequestSend(ServerRequestModel request) throws Exception;
}
