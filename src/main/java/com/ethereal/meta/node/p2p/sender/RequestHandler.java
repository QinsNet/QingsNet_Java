package com.ethereal.meta.node.p2p.sender;
import com.ethereal.meta.core.entity.Error;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.request.core.RequestContext;
import com.ethereal.meta.util.SerializeUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

public class RequestHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    RequestContext context;
    Meta meta;
    static ExecutorService es = new NioEventLoopGroup(1);
    public RequestHandler(Meta meta,RequestContext context) {
        this.meta = meta;
        this.context = context;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse res) throws TrackException {
        ResponseMeta responseMeta = new ResponseMeta();
        responseMeta.setMapping(context.getRequest().getMapping());
        responseMeta.setError(SerializeUtil.gson.fromJson(res.headers().get("error"), Error.class));
        responseMeta.setProtocol(res.headers().get("protocol"));
        //instance 处理
        //省略
        ///////
        responseMeta.setInstance(responseMeta.getInstance());
        responseMeta.setResult(res.content().toString(StandardCharsets.UTF_8));
        context.setResult(responseMeta);
        es.submit(() -> this.meta.getRequest().receive(context));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        meta.onException(new Exception(cause));
    }

}
