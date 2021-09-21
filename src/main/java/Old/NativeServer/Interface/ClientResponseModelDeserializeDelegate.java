package Old.NativeServer.Interface;

import Model.ClientResponseModel;

public interface ClientResponseModelDeserializeDelegate {
    //客户端响应模型被反序列化
    ClientResponseModel Deserialize(String obj);
}
