package com.ethereal.meta.net.p2p.recevier;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.p2p.sender.RemoteInfo;
import com.ethereal.meta.service.core.ServiceContext;
import com.ethereal.meta.util.SerializeUtil;
import com.ethereal.meta.util.UrlUtil;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

public class ServiceHandler extends SimpleChannelInboundHandler<FullHttpRequest>  {
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    static final Type gson_type = new TypeToken<HashMap<String,String>>(){}.getType();
    private final Meta root;
    public ServiceHandler(ExecutorService executorService, Meta root) {
        this.es = executorService;
        this.root = root;
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
        URL url = new URL(req.uri());
        //处理请求头,生成请求元数据
        RequestMeta requestMeta = new RequestMeta();
        requestMeta.setMapping(url.getPath());
        requestMeta.setParams(UrlUtil.getQuery(url.getQuery()));
        requestMeta.setProtocol(req.headers().get("protocol"));
        requestMeta.setMeta(req.headers().get("meta"));
        requestMeta.setHost(req.headers().get("host"));
        requestMeta.setPort(req.headers().get("port"));
        Meta meta = root;
        LinkedList<String> mappings = new LinkedList<>(Arrays.asList(requestMeta.getMapping().split("/")));
        mappings.removeLast();
        for(String name : mappings){
            if (meta.getMetas().containsKey(name)){
                meta = meta.getMetas().get(name);
            }
            else {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.NOT_FOUND,Unpooled.copiedBuffer(String.format("Meta:%s 未找到", requestMeta.getMapping()),StandardCharsets.UTF_8)));
                return;
            }
        }
        ServiceContext context = new ServiceContext();
        context.setRequestMeta(requestMeta);
        context.setRemoteInfo(new RemoteInfo(new InetSocketAddress(requestMeta.getHost(),Integer.parseInt(requestMeta.getPort()))));
        context.setInstance(meta.newInstance(context.getRemoteInfo()));
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
            send(finalMeta.getService().receive(context));
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        root.onException(new Exception(cause));
    }

    public boolean send(Object data) {
        if(data == null){
            return true;
        }
        else if(data instanceof ResponseMeta){
            ResponseMeta responseMeta = (ResponseMeta) data;
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(SerializeUtil.gson.toJson(responseMeta.getResult()).getBytes(StandardCharsets.UTF_8)));
            response.headers().set("error", SerializeUtil.gson.toJson(responseMeta.getError()));
            response.headers().set("protocol",responseMeta.getProtocol());
            response.headers().set("meta",responseMeta.getMeta());
            response.headers().set("value",responseMeta.getMapping());
            send(response);
        }
        else if(data instanceof RequestMeta){
            RequestMeta requestMeta = (RequestMeta) data;
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(SerializeUtil.gson.toJson(requestMeta.getParams()).getBytes(StandardCharsets.UTF_8)));
            response.headers().set("protocol", requestMeta.getProtocol());
            response.headers().set("meta", requestMeta.getMeta());
            response.headers().set("value", requestMeta.getMapping());
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

    private void send(DefaultFullHttpResponse res) {
        ctx.channel().writeAndFlush(res);
    }
}
