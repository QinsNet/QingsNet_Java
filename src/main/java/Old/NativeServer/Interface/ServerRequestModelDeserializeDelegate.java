package Old.NativeServer.Interface;

import Model.ServerRequestModel;

public interface ServerRequestModelDeserializeDelegate {
    //服务器请求模型反序列化
    ServerRequestModel Deserialize(String obj);
}
