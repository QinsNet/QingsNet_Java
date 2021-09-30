package com.ethereal.server.Server.Abstract;

import com.ethereal.server.Core.Model.ClientRequestModel;
import com.ethereal.server.Core.Model.ClientResponseModel;
import com.ethereal.server.Core.Model.ServerRequestModel;
import com.ethereal.server.Server.Delegate.ClientRequestModelDeserializeDelegate;
import com.ethereal.server.Server.Delegate.ClientResponseModelSerializeDelegate;
import com.ethereal.server.Server.Delegate.ServerRequestModelSerializeDelegate;
import com.ethereal.server.Utils.Utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ServerConfig {
    private Charset charset = StandardCharsets.UTF_8;
    protected ClientResponseModelSerializeDelegate clientResponseModelSerialize;
    protected ClientRequestModelDeserializeDelegate clientRequestModelDeserialize;
    protected ServerRequestModelSerializeDelegate serverRequestModelSerialize;
    protected int processTimeout = 10000;


    public int getProcessTimeout() {
        return processTimeout;
    }

    public void setProcessTimeout(int processTimeout) {
        this.processTimeout = processTimeout;
    }

    public ServerConfig(){
        //模型=>类=>实例化类（实体)=>数据(字节、文本）【序列化】=>发送
        clientResponseModelSerialize = obj -> Utils.gson.toJson(obj, ClientResponseModel.class);
        clientRequestModelDeserialize = obj -> Utils.gson.fromJson(obj, ClientRequestModel.class);
        serverRequestModelSerialize = obj -> Utils.gson.toJson(obj, ServerRequestModel.class);
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public ClientResponseModelSerializeDelegate getClientResponseModelSerialize() {
        return clientResponseModelSerialize;
    }

    public void setClientResponseModelSerialize(ClientResponseModelSerializeDelegate clientResponseModelSerialize) {
        this.clientResponseModelSerialize = clientResponseModelSerialize;
    }

    public ClientRequestModelDeserializeDelegate getClientRequestModelDeserialize() {
        return clientRequestModelDeserialize;
    }

    public void setClientRequestModelDeserialize(ClientRequestModelDeserializeDelegate clientRequestModelDeserialize) {
        this.clientRequestModelDeserialize = clientRequestModelDeserialize;
    }

    public ServerRequestModelSerializeDelegate getServerRequestModelSerialize() {
        return serverRequestModelSerialize;
    }

    public void setServerRequestModelSerialize(ServerRequestModelSerializeDelegate serverRequestModelSerialize) {
        this.serverRequestModelSerialize = serverRequestModelSerialize;
    }
}
