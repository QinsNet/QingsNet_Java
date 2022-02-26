package com.qins.net.node.http.recevier;
import com.qins.net.core.console.Console;
import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.meta.core.MetaClassLoader;
import com.qins.net.service.core.ServiceContext;
import com.qins.net.util.Http2Util;
import com.qins.net.util.SerializeUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import jdk.internal.dynalink.support.ClassMap;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class ServiceHandler extends SimpleChannelInboundHandler<FullHttpRequest>  {
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    private final MetaClassLoader classLoader;
    private final NodeAddress local;
    public ServiceHandler(ExecutorService executorService, MetaClassLoader classLoader, NodeAddress local) {
        this.es = executorService;
        this.classLoader = classLoader;
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
            //处理请求头,生成请求元数据
            RequestMeta requestMeta = new RequestMeta();
            requestMeta.setMapping(uri.getPath());
            requestMeta.setProtocol(req.headers().get("protocol"));
            if (req.headers().contains("instance")){
                requestMeta.setInstance(URLDecoder.decode(req.headers().get("instance"),"UTF-8"));
            }
            ServiceContext context = new ServiceContext();
            context.setMappings(new LinkedList<>(Arrays.asList(requestMeta.getMapping().split("/"))));
            if(!context.getMappings().isEmpty()) context.getMappings().removeFirst();
            context.setRequestMeta(requestMeta);

            //获取参数
            requestMeta.setParams(Http2Util.getURLParamsFromChannel(req));
            if (req.method() == HttpMethod.GET) {

            }
            else if (req.method() == HttpMethod.POST) {
                Map<String ,String > params = Http2Util.getBodyParamsChannel(req);
                if(params != null)requestMeta.getParams().putAll(params);
            }
            else {
                send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((String.format("%s请求不支持", req.method())),StandardCharsets.UTF_8)));
                return;
            }
            if(requestMeta.getParams() == null)requestMeta.setParams(new HashMap<>());
            MetaClass metaClass = classLoader.getMetaClass(context.getMappings().pop());
            if(metaClass == null){
                send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((String.format("%s请求类未找到", requestMeta.getMapping())),StandardCharsets.UTF_8)));
                return;
            }
            es.submit(() -> {
                send(metaClass.getService().receive(context));
            });
        }
        catch (Exception e){
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.EXPECTATION_FAILED,Unpooled.copiedBuffer(Arrays.toString(e.getStackTrace()).getBytes(StandardCharsets.UTF_8))));
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.EXPECTATION_FAILED,Unpooled.copiedBuffer(Arrays.toString(cause.getStackTrace()).getBytes(StandardCharsets.UTF_8))));
        ctx.close();
    }

    public boolean send(Object data) {
        if(data == null){
            return true;
        }
        else if(data instanceof ResponseMeta){
            ResponseMeta responseMeta = (ResponseMeta) data;
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,Unpooled.copiedBuffer(SerializeUtil.gson.toJson(responseMeta.getResult()).getBytes(StandardCharsets.UTF_8)));
            response.headers().set("protocol",responseMeta.getProtocol());
            try {
                if(responseMeta.getException() != null){
                    response.headers().set("exception",URLEncoder.encode(responseMeta.getException(),"UTF-8"));
                }
                else {
                    response.headers().set("instance", URLEncoder.encode(responseMeta.getInstance(),"UTF-8"));
                    response.headers().set("params", URLEncoder.encode(SerializeUtil.gson.toJson(responseMeta.getParams()),"UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                Console.error(e.getMessage());
            }
            send(response);
            ctx.close();
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
    }
}
