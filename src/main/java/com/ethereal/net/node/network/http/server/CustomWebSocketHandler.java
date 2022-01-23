package com.ethereal.net.node.network.http.server;
import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.core.entity.ResponseMeta;
import com.ethereal.net.net.core.Net;
import com.ethereal.net.node.core.Node;
import com.ethereal.net.node.network.INetwork;
import com.ethereal.net.service.core.Service;
import com.ethereal.net.utils.UrlUtils;
import com.ethereal.net.utils.Utils;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class CustomWebSocketHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements INetwork{
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    private final Type gson_type = new TypeToken<HashMap<String,String>>(){}.getType();
    private Node node;
    private Service service;
    public CustomWebSocketHandler(ExecutorService executorService,Service service){
        this.es = executorService;
        this.service = service;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        node.onClose();
        node.setNetwork(null);
        ctx = null;
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        URL url = new URL(req.uri());
        RequestMeta requestMeta = new RequestMeta();
        for(String name:url.getPath().split("/")){
            if(service.getServices().containsKey(name)){
                service = service.getServices().get(name);
            }
            else if(service != null && service.getMethods().containsKey(name)){
                requestMeta.setMethod(service.getMethods().get(name));
            }
            else {
                send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer((String.format("%s 服务未找到", url.getPath())),StandardCharsets.UTF_8)));
                return;
            }
        }
        assert service != null;
        //处理请求头,生成请求元数据
        requestMeta.setService(service);
        requestMeta.setMapping(url.getPath());
        requestMeta.setParams(UrlUtils.getQuery(url.getQuery()));
        requestMeta.setId(req.headers().get("id"));
        requestMeta.setProtocol(req.headers().get("protocol"));
        //Body体中获取参数
        if(req.method() == HttpMethod.POST){
            String raw_body = req.content().toString(service.getConfig().getCharset());
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
        //如果是第一次请求这个服务，生成Node
        if(req.headers().get("node") != null){
            Node node = service.createNode(req.headers().get("node"));
            node.setNetwork(this);
            node.onStart();
        }
        es.submit(() -> {
            send(service.receiveProcess(requestMeta));
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
