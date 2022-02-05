package com.ethereal.meta.net.network.http.client;
import com.ethereal.meta.core.entity.Error;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.INetwork;
import com.ethereal.meta.util.SerializeUtil;
import com.ethereal.meta.util.Util;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

public class CustomHandler extends SimpleChannelInboundHandler<FullHttpResponse> implements INetwork {
    private final ExecutorService es;
    static final Type gson_type = new TypeToken<HashMap<String,String>>(){}.getType();
    private ChannelHandlerContext ctx;
    private final Meta rootMeta;
    public CustomHandler(ExecutorService executorService,Meta rootMeta){
        this.es = executorService;
        this.rootMeta = rootMeta;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        rootMeta.getNet().updateNetwork(this);
        rootMeta.getNet().onConnectSuccess();
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        rootMeta.getNet().onConnectLost();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse res) throws TrackException {
        String protocol = res.headers().get("protocol");
        if("Meta-Request-1.0".equals(protocol)){
            //处理请求头,生成请求元数据
            RequestMeta requestMeta = new RequestMeta();
            requestMeta.setId(res.headers().get("id"));
            requestMeta.setProtocol(res.headers().get("protocol"));
            requestMeta.setMapping(res.headers().get("mapping"));
            requestMeta.setMeta(res.headers().get("meta"));
            Meta meta = rootMeta;
            LinkedList<String> mappings = new LinkedList<>(Arrays.asList(requestMeta.getMapping().split("/")));
            mappings.removeLast();
            for(String name : mappings){
                if (meta.getMetas().containsKey(name)){
                    meta = meta.getMetas().get(name);
                }
                else {
                    send(new ResponseMeta(requestMeta,new Error(Error.ErrorCode.NotFoundNet,String.format("Meta:%s 未找到", requestMeta.getMapping()))));
                    return;
                }
            }
            //Body体中获取参数
            String raw_body = res.content().toString(StandardCharsets.UTF_8);
            HashMap<String,String> body = SerializeUtil.gson.fromJson(raw_body,gson_type);
            for (String key : body.keySet()){
                if(!requestMeta.getParams().containsKey(key)){
                    requestMeta.getParams().put(key,body.get(key));
                }
            }
            Meta finalMeta = meta;
            es.submit(() -> {
                send(finalMeta.getService().receive(requestMeta));
            });
        }
        else if("Meta-Response-1.0".equals(protocol)){
            ResponseMeta responseMeta = new ResponseMeta();
            responseMeta.setMapping(res.headers().get("mapping"));
            responseMeta.setId(res.headers().get("id"));
            responseMeta.setError(SerializeUtil.gson.fromJson(res.headers().get("error"), Error.class));
            responseMeta.setProtocol(protocol);
            responseMeta.setMeta(res.headers().get("rootMeta"));
            Meta meta = rootMeta;
            LinkedList<String> mappings = new LinkedList<>(Arrays.asList(responseMeta.getMapping().split("/")));
            mappings.removeLast();
            for(String name : mappings){
                if (meta.getMetas().containsKey(name)){
                    meta = meta.getMetas().get(name);
                }
                else {
                    throw new TrackException(TrackException.ExceptionCode.NotFoundMeta,String.format("Meta:%s 未找到", responseMeta.getMapping()));
                }
            }
            responseMeta.setResult(res.content().toString(StandardCharsets.UTF_8));
            Meta finalMeta = meta;
            es.submit(() -> finalMeta.getRequest().receive(responseMeta));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        rootMeta.onException(new Exception(cause));
    }

    @Override
    public boolean start() {
        return true;
    }
    private void send(DefaultFullHttpRequest res) {
        ctx.channel().writeAndFlush(res);
    }
    @Override
    public boolean send(Object data) {
        if(data == null){
            return true;
        }
        else if(data instanceof ResponseMeta){
            ResponseMeta responseMeta = (ResponseMeta) data;
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.POST,responseMeta.getMapping(),Unpooled.copiedBuffer(SerializeUtil.gson.toJson(responseMeta.getResult()).getBytes(StandardCharsets.UTF_8)));
            request.headers().set("id",responseMeta.getId());
            request.headers().set("error", SerializeUtil.gson.toJson(responseMeta.getError()));
            request.headers().set("protocol",responseMeta.getProtocol());
            request.headers().set("meta",responseMeta.getMeta());
            request.headers().set("mapping",responseMeta.getMapping());
            send(request);
        }
        else if(data instanceof RequestMeta){
            RequestMeta requestMeta = (RequestMeta) data;
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.POST,requestMeta.getMapping(),Unpooled.copiedBuffer(SerializeUtil.gson.toJson(requestMeta.getParams()).getBytes(StandardCharsets.UTF_8)));
            request.headers().set("id", requestMeta.getId());
            request.headers().set("protocol", requestMeta.getProtocol());
            request.headers().set("meta", requestMeta.getMeta());
            request.headers().set("mapping", requestMeta.getMapping());
            send(request);
        }
        else if(data instanceof byte[]){
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((byte[]) data)));
        }
        else if(data instanceof DefaultFullHttpResponse){
            send((DefaultFullHttpResponse) data);
        }
        else if(data instanceof String){
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((String) data,StandardCharsets.UTF_8)));
        }
        return true;
    }

    @Override
    public boolean close() {
        return false;
    }
}
