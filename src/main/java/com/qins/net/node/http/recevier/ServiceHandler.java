package com.qins.net.node.http.recevier;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.meta.core.MetaClassLoader;
import com.qins.net.util.SerializeUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class ServiceHandler extends SimpleChannelInboundHandler<FullHttpRequest>  {
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    private final HashMap<String,MetaClass> metas;
    private final NodeAddress local;
    public ServiceHandler(ExecutorService executorService, HashMap<String,MetaClass> metas, NodeAddress local) {
        this.es = executorService;
        this.metas = metas;
        this.local = local;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        try {
            URI uri = new URI(req.uri());
            RequestMeta requestMeta;
            if (req.method() == HttpMethod.GET) {
                send(new ResponseMeta("不支持Get方法"));
                return;
            }
            else if (req.method() == HttpMethod.POST) {
                String a = req.content().toString(StandardCharsets.UTF_8);
                requestMeta = SerializeUtil.gson.fromJson(a,RequestMeta.class);
            }
            else {
                send(new ResponseMeta((String.format("%s请求不支持", req.method()))));
                return;
            }
            requestMeta.setMapping(uri.getPath()).setProtocol(req.headers().get("protocol"));
            if(requestMeta.getParams() == null)requestMeta.setParams(new HashMap<>());
            MetaClass metaClass = metas.get(requestMeta.getMapping().split("/")[1]);
            if(metaClass == null){
                send(new ResponseMeta((String.format("%s请求类未找到", requestMeta.getMapping()))));
                return;
            }
            es.submit(() -> {
                send(metaClass.getService().receive(requestMeta));
            });
        }
        catch (Exception e){
            send(new ResponseMeta(e));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        send(new ResponseMeta(new Exception(cause)));
        super.exceptionCaught(ctx, cause);
    }

    public boolean send(Object data) {
        if(data == null){
            return true;
        }
        else if(data instanceof ResponseMeta){
            ResponseMeta responseMeta = (ResponseMeta) data;
            String body = SerializeUtil.gson.toJson(responseMeta);
            System.out.println(body);
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(body.getBytes(StandardCharsets.UTF_8)));
            send(response);
            Console.debug(data.toString());
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

    private void send(DefaultFullHttpResponse res) {
        ctx.writeAndFlush(res);
        ctx.close();
    }
}
