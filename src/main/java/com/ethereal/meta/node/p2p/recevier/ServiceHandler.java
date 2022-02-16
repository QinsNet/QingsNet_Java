package com.ethereal.meta.node.p2p.recevier;
import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.core.entity.NodeAddress;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.service.core.ServiceContext;
import com.ethereal.meta.util.SerializeUtil;
import com.ethereal.meta.util.UrlUtil;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Type;
import java.net.URI;
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
    private final NodeAddress local;
    public ServiceHandler(ExecutorService executorService, Meta root, NodeAddress local) {
        this.es = executorService;
        this.root = root;
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
        URI uri = new URI(req.uri());
        //处理请求头,生成请求元数据
        RequestMeta requestMeta = new RequestMeta();
        requestMeta.setMapping(uri.getPath());
        requestMeta.setParams(UrlUtil.getQuery(uri.getQuery()));
        requestMeta.setProtocol(req.headers().get("protocol"));
        requestMeta.setMeta(req.headers().get("meta"));
        requestMeta.setHost(req.headers().get("host"));
        requestMeta.setPort(req.headers().get("port"));
        ServiceContext context = new ServiceContext();
        context.setLocal(local);
        context.setMappings(new LinkedList<>(Arrays.asList(requestMeta.getMapping().split("/"))));
        context.getMappings().removeFirst();
        context.setRequestMeta(requestMeta);
        //Body体中获取参数
        if(req.method() == HttpMethod.GET){

        }
        else if(req.method() == HttpMethod.POST){
            String raw_body = req.content().toString(StandardCharsets.UTF_8);
            HashMap<String,String> body = SerializeUtil.gson.fromJson(raw_body,gson_type);
            if(body != null){
                for (String key : body.keySet()){
                    if(!requestMeta.getParams().containsKey(key)){
                        requestMeta.getParams().put(key,body.get(key));
                    }
                }
            }
        }
        else {
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((String.format("%s请求不支持", req.method())),StandardCharsets.UTF_8)));
        }
        es.submit(() -> {
            send(root.getService().receive(context));
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        root.onException(new Exception(cause));
        super.exceptionCaught(ctx, cause);
    }

    public boolean send(Object data) {
        if(data == null){
            return true;
        }
        else if(data instanceof ResponseMeta){
            Console.debug(data.toString());
            ResponseMeta responseMeta = (ResponseMeta) data;
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(SerializeUtil.gson.toJson(responseMeta.getResult()).getBytes(StandardCharsets.UTF_8)));
            response.headers().set("error", SerializeUtil.gson.toJson(responseMeta.getError()));
            response.headers().set("protocol",responseMeta.getProtocol());
            response.headers().set("meta",responseMeta.getMeta());
            send(response);
            ctx.close();
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
