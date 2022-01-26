package com.ethereal.meta.net.network.http.client;
import com.ethereal.meta.core.entity.Error;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.net.network.Network;
import com.ethereal.meta.utils.UrlUtils;
import com.ethereal.meta.utils.Utils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class CustomHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    private final Meta meta;
    public CustomHandler(ExecutorService executorService,Meta meta){
        this.es = executorService;
        this.meta = meta;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        meta.onConnectSuccess();
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        meta.onConnectLost();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse res) {
        String protocol = res.headers().get("protocol");
        if("Meta-Request-1.0".equals(protocol)){
            RequestMeta requestMeta = new RequestMeta();
            requestMeta.setProtocol(protocol);
            requestMeta.setId(res.headers().get("id"));
            requestMeta.setMapping(res.headers().get("mapping"));
        }
        else if("Meta-Response-1.0".equals(protocol)){
            ResponseMeta responseMeta = new ResponseMeta();
            responseMeta.setId(res.headers().get("id"));
            responseMeta.setError(Utils.gson.fromJson(res.headers().get("error"),Error.class));
            responseMeta.setResult(res.content().toString(meta.getRequestConfig()));
        }
        URL url = new URL(res);
        //处理请求头,生成请求元数据
        resuestMeta resuestMeta = new resuestMeta();
        resuestMeta.setMapping(url.getPath());
        resuestMeta.setParams(UrlUtils.getQuery(url.getQuery()));
        resuestMeta.setId(res.headers().get("id"));
        resuestMeta.setProtocol(res.headers().get("protocol"));
        resuestMeta.setMapping(res.headers().get("mapping"));
        boolean search;
        for(String name:url.getPath().split("/")){
            search = false;
            for (Field field : metaClass.getFields()){
                if(field.getAnnotation(MetaMapping.class) != null && Objects.equals(field.getAnnotation(MetaMapping.class).mapping(), name)){
                    metaClass = (Class<? extends Meta>) field.getType();
                    search = true;
                    break;
                }
            }
            if(!search){
                send(new ResponseMeta(resuestMeta.getId(),new Error(Error.ErrorCode.NotFoundNet,String.format("Meta:%s 未找到", url.getPath()))));
                return;
            }
        }
        resuestMeta.setMetaClass(metaClass);
        //构造Meta
        final Meta meta = Meta.connect(metaClass);
        meta.setNetwork(this);
        //Body体中获取参数
        if(res.method() == HttpMethod.GET){

        }
        else if(res.method() == HttpMethod.POST){
            String raw_body = res.content().toString(meta.getNetConfig().getCharset());
            HashMap<String,String> body = Utils.gson.fromJson(raw_body,gson_type);
            for (String key : body.keySet()){
                if(!resuestMeta.getParams().containsKey(key)){
                    resuestMeta.getParams().put(key,body.get(key));
                }
            }
        }
        else {
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer((String.format("%s请求不支持", res.method())), StandardCharsets.UTF_8)));
        }
        meta.update(res.headers().get("meta"));
        es.submit(() -> {
            meta.onConnectSuccess();
            send(meta.receiveProcess(resuestMeta));
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        meta.onException(new Exception(cause));
    }
}
