package com.ethereal.meta.net.network.http.client;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.Network;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponse;

import java.util.concurrent.ExecutorService;

public class CustomHandler extends SimpleChannelInboundHandler<HttpResponse> {
    private final ExecutorService es;
    private ChannelHandlerContext ctx;
    private final Meta meta;
    public CustomHandler(ExecutorService executorService,Meta meta){
        this.es = executorService;
        this.meta = meta;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        meta.onConnectSuccess();
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        meta.onConnectLost();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpResponse httpResponse) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        meta.onException(new Exception(cause));
    }
}
