package com.ethereal.meta.net.network.http.server;
import com.ethereal.meta.core.entity.Error;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.INetwork;
import com.ethereal.meta.util.SerializeUtil;
import com.ethereal.meta.util.UrlUtil;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

public class CustomHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements INetwork {
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    static final Type gson_type = new TypeToken<HashMap<String,String>>(){}.getType();
    private final Meta rootMeta;
    public CustomHandler(ExecutorService executorService, Class<?> instance) {
        this.es = executorService;
        this.rootMeta = Meta.newInstance(instance);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        rootMeta.getNet().updateNetwork(this);
        rootMeta.getNet().onConnectSuccess();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        rootMeta.getNet().onConnectLost();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String protocol = req.headers().get("protocol");
        URL url = new URL(req.uri());
        if("Meta-Request-1.0".equals(protocol)){
            //处理请求头,生成请求元数据
            RequestMeta requestMeta = new RequestMeta();
            requestMeta.setMapping(url.getPath());
            requestMeta.setParams(UrlUtil.getQuery(url.getQuery()));
            requestMeta.setId(req.headers().get("id"));
            requestMeta.setProtocol(req.headers().get("protocol"));
            requestMeta.setMeta(req.headers().get("meta"));
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
            if(req.method() == HttpMethod.GET){

            }
            else if(req.method() == HttpMethod.POST){
                String raw_body = req.content().toString(StandardCharsets.UTF_8);
                HashMap<String,String> body = SerializeUtil.gson.fromJson(raw_body,gson_type);
                for (String key : body.keySet()){
                    if(!requestMeta.getParams().containsKey(key)){
                        requestMeta.getParams().put(key,body.get(key));
                    }
                }
            }
            else {
                send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((String.format("%s请求不支持", req.method())),StandardCharsets.UTF_8)));
            }
            Meta finalMeta = meta;
            es.submit(() -> {
                send(finalMeta.getService().receive(requestMeta));
            });
        }
        else if("Meta-Response-1.0".equals(protocol)){
            ResponseMeta responseMeta = new ResponseMeta();
            responseMeta.setMapping(url.getPath());
            responseMeta.setId(req.headers().get("id"));
            responseMeta.setError(SerializeUtil.gson.fromJson(req.headers().get("error"), Error.class));
            responseMeta.setProtocol(protocol);
            responseMeta.setMeta(req.headers().get("meta"));
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
            responseMeta.setResult(req.content().toString(StandardCharsets.UTF_8));
            Meta finalMeta = meta;
            es.submit(() -> finalMeta.getRequest().receive(responseMeta));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        rootMeta.onException(new Exception(cause));
    }

    @Override
    public boolean start() {
        return true;
    }

    private void send(DefaultFullHttpResponse res) {
        ctx.channel().writeAndFlush(res);
    }

    @Override
    public boolean send(Object data) {
        if(data == null){
            return true;
        }
        else if(data instanceof ResponseMeta){
            ResponseMeta responseMeta = (ResponseMeta) data;
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(SerializeUtil.gson.toJson(responseMeta.getResult()).getBytes(StandardCharsets.UTF_8)));
            response.headers().set("id",responseMeta.getId());
            response.headers().set("error", SerializeUtil.gson.toJson(responseMeta.getError()));
            response.headers().set("protocol",responseMeta.getProtocol());
            response.headers().set("meta",responseMeta.getMeta());
            response.headers().set("mapping",responseMeta.getMapping());
            send(response);
        }
        else if(data instanceof RequestMeta){
            RequestMeta requestMeta = (RequestMeta) data;
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(SerializeUtil.gson.toJson(requestMeta.getParams()).getBytes(StandardCharsets.UTF_8)));
            response.headers().set("id", requestMeta.getId());
            response.headers().set("protocol", requestMeta.getProtocol());
            response.headers().set("meta", requestMeta.getMeta());
            response.headers().set("mapping", requestMeta.getMapping());
            send(response);
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
        ctx.close();
        return true;
    }
}
