package com.ethereal.meta.net.network.http.server;
import com.ethereal.meta.core.entity.Error;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.net.network.Network;
import com.ethereal.meta.utils.UrlUtils;
import com.ethereal.meta.utils.Utils;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class CustomHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements Network{
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    private final Type gson_type = new TypeToken<HashMap<String,String>>(){}.getType();
    private Class<? extends Meta> metaClass;
    public CustomHandler(ExecutorService executorService, Class<? extends Meta> metaClass){
        this.es = executorService;
        this.metaClass = metaClass;
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
        requestMeta.setParams(UrlUtils.getQuery(url.getQuery()));
        requestMeta.setId(req.headers().get("id"));
        requestMeta.setProtocol(req.headers().get("protocol"));
        requestMeta.setMapping(req.headers().get("mapping"));
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
                send(new ResponseMeta(requestMeta.getId(),new Error(Error.ErrorCode.NotFoundNet,String.format("Meta:%s 未找到", url.getPath()))));
                return;
            }
        }
        requestMeta.setMetaClass(metaClass);
        //构造Meta
        final Meta meta = Meta.connect(metaClass);
        meta.setNetwork(this);
        //Body体中获取参数
        if(req.method() == HttpMethod.GET){

        }
        else if(req.method() == HttpMethod.POST){
            String raw_body = req.content().toString(meta.getNetConfig().getCharset());
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
        meta.update(req.headers().get("meta"));
        es.submit(() -> {
            meta.onConnectSuccess();
            send(meta.receiveProcess(requestMeta));
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
