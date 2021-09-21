package Old.RPCNet.Interface;

import Model.ClientResponseModel;
import Model.RPCException;

public interface IClientResponseSend {
    public void clientResponseSend(ClientResponseModel request) throws RPCException;
}
