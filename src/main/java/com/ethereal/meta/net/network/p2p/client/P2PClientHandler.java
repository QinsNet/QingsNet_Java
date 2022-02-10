package com.ethereal.meta.net.network.p2p.client;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.meta.Meta;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class P2PClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    Meta meta;
    public P2PClientHandler(Meta meta){
        this.meta = meta;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse res) throws TrackException {
        System.out.println("客户端请求服务端的报错信息：" + res.content().toString(StandardCharsets.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        meta.onException(new Exception(cause));
    }

}
