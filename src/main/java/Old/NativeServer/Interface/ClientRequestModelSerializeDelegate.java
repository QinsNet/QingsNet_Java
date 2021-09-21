package Old.NativeServer.Interface;

import Model.ClientRequestModel;

public interface ClientRequestModelSerializeDelegate {
    //序列列化客户端请求模型
    String Serialize(ClientRequestModel obj);
}

