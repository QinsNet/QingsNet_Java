package Net.Interface;

import Old.Model.ClientRequestModel;
import Old.Model.ClientResponseModel;

public interface INet {
    public boolean Publish();
    public ClientResponseModel ClientRequestReceieveProcess(ClientRequestModel request);
}
