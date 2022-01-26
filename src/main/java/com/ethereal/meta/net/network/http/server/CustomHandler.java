package com.ethereal.meta.net.network.http.server;
import com.ethereal.meta.core.entity.Error;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.Network;
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

public class CustomHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements Network{
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    private final Type gson_type = new TypeToken<HashMap<String,String>>(){}.getType();
    private final Meta rootMeta;
    public CustomHandler(ExecutorService executorService, Class<? extends Meta> rootMetaClass) throws IllegalAccessException {
        this.es = executorService;
        this.rootMeta = Meta.connect(rootMetaClass);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        rootMeta.onNetwork(this);
        rootMeta.onConnectSuccess();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        rootMeta.onConnectLost();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String protocol = req.headers().get("protocol");
        if("Meta-Request-1.0".equals(protocol)){
            URL url = new URL(req.uri());
            //处理请求头,生成请求元数据
            RequestMeta requestMeta = new RequestMeta();
            requestMeta.setMapping(url.getPath());
            requestMeta.setParams(UrlUtils.getQuery(url.getQuery()));
            requestMeta.setId(req.headers().get("id"));
            requestMeta.setProtocol(req.headers().get("protocol"));
            requestMeta.setMapping(req.headers().get("mapping"));
            requestMeta.setMeta(req.headers().get("meta"));
            Meta meta = rootMeta;
            for(String name:requestMeta.getMapping().split("/")){
                if (meta.getMetas().containsKey(name)){
                    meta = meta.getMetas().get(name);
                }
                else {
                    send(new ResponseMeta(requestMeta.getId(),new Error(Error.ErrorCode.NotFoundNet,String.format("Meta:%s 未找到", requestMeta.getMapping()))));
                    return;
                }
            }
            meta.setNetwork(this);
            //Body体中获取参数
            if(req.method() == HttpMethod.GET){

            }
            else if(req.method() == HttpMethod.POST){
                String raw_body = req.content().toString(StandardCharsets.UTF_8);
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
            Meta finalMeta = meta;
            es.submit(() -> {
                send(finalMeta.receiveProcess(requestMeta));
            });
        }
        else if("Meta-Response-1.0".equals(protocol)){
            ResponseMeta responseMeta = new ResponseMeta();
            responseMeta.setMapping(req.headers().get("mapping"));
            responseMeta.setId(req.headers().get("id"));
            responseMeta.setError(Utils.gson.fromJson(req.headers().get("error"), Error.class));
            responseMeta.setProtocol(protocol);
            responseMeta.setMeta(req.headers().get("meta"));
            Meta meta = rootMeta;
            for(String name:responseMeta.getMapping().split("/")){
                if (meta.getMetas().containsKey(name)){
                    meta = meta.getMetas().get(name);
                }
                else {
                    send(new ResponseMeta(responseMeta.getId(),new Error(Error.ErrorCode.NotFoundNet,String.format("Meta:%s 未找到", responseMeta.getMapping()))));
                    return;
                }
            }
            responseMeta.setResult(req.content().toString(StandardCharsets.UTF_8));
            Meta finalMeta = meta;
            es.submit(() -> {
                send(finalMeta.receiveProcess(requestMeta));
            });
        }
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
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(Utils.gson.toJson(responseMeta.getResult()).getBytes(StandardCharsets.UTF_8)));
            response.headers().set("id",responseMeta.getId());
            response.headers().set("error",Utils.gson.toJson(responseMeta.getError()));
            response.headers().set("protocol",responseMeta.getProtocol());
            response.headers().set("meta",responseMeta.getMeta());
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
