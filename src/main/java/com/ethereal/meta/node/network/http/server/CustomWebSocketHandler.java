package com.ethereal.meta.node.network.http.server;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.node.network.INetwork;
import com.ethereal.meta.utils.UrlUtils;
import com.ethereal.meta.utils.Utils;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class CustomWebSocketHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements INetwork{
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    private final Type gson_type = new TypeToken<HashMap<String,String>>(){}.getType();
    private Meta meta;
    public CustomWebSocketHandler(ExecutorService executorService, Meta meta){
        this.es = executorService;
        this.meta = meta;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        meta.onClose();
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        URL url = new URL(req.uri());
        RequestMeta requestMeta = new RequestMeta();
        //这里考虑到每一个连接都会产生一个独立的Meta，会造成一定程度的浪费，不过目前基于构想型设计，缓存等之类的技术后续考虑是否加入。
        for(String name:url.getPath().split("/")){
            if(meta.getMetas().containsKey(name)){
                meta = meta.getMetas().get(name);
            }
            else if(meta != null && meta.getMethods().containsKey(name)){

                //找到了方法
                requestMeta.setMethod(meta.getMethods().get(name));
            }
            else {
                send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer((String.format("%s 服务未找到", url.getPath())),StandardCharsets.UTF_8)));
                return;
            }
        }
        //处理请求头,生成请求元数据
        requestMeta.setMapping(url.getPath());
        requestMeta.setParams(UrlUtils.getQuery(url.getQuery()));
        requestMeta.setId(req.headers().get("id"));
        requestMeta.setProtocol(req.headers().get("protocol"));
        //Body体中获取参数
        if(req.method() == HttpMethod.POST){
            String raw_body = req.content().toString(meta.getNodeConfig().getCharset());
            HashMap<String,String> body = Utils.gson.fromJson(raw_body,gson_type);
            for (String key : body.keySet()){
                if(!requestMeta.getParams().containsKey(key)){
                    requestMeta.getParams().put(key,body.get(key));
                }
            }
        }
        else {
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((String.format("%s请求不支持", req.method())),StandardCharsets.UTF_8)));
        }
        if(req.headers().get("meta") != null){
            meta.update(req.headers().get("meta"));
        }
        es.submit(() -> {
            send(serviceNet.receiveProcess(requestMeta));
        });
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
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(Utils.gson.toJson(data).getBytes(StandardCharsets.UTF_8))));
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
