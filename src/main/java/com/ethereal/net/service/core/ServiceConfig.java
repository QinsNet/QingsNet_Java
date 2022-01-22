package com.ethereal.net.service.core;

import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.core.entity.ResponseMeta;
import com.ethereal.net.node.delegate.ClientRequestModelDeserializeDelegate;
import com.ethereal.net.node.delegate.ClientResponseModelSerializeDelegate;
import com.ethereal.net.node.delegate.ServerRequestModelSerializeDelegate;
import com.ethereal.net.utils.Utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @ProjectName: YiXian_Client
 * @Package: com.yixian.material.RPC
 * @ClassName: RPCNetServiceConfig
 * @Description: java类作用描述
 * @Author: Jianxian
 * @CreateDate: 2021/3/5 17:47
 * @UpdateUser: Jianxian
 * @UpdateDate: 2021/3/5 17:47
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ServiceConfig {
    private Charset charset = StandardCharsets.UTF_8;
    protected ClientResponseModelSerializeDelegate clientResponseModelSerialize;
    protected ClientRequestModelDeserializeDelegate clientRequestModelDeserialize;
    protected ServerRequestModelSerializeDelegate serverRequestModelSerialize;

    public ServiceConfig(){
        //模型=>类=>实例化类（实体)=>数据(字节、文本）【序列化】=>发送
        clientResponseModelSerialize = obj -> Utils.gson.toJson(obj, ResponseMeta.class);
        clientRequestModelDeserialize = obj -> Utils.gson.fromJson(obj, RequestMeta.class);
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
