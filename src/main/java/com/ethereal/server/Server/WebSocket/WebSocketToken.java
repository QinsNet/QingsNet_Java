package com.ethereal.server.Server.WebSocket;

import com.ethereal.server.Core.Model.ClientResponseModel;
import com.ethereal.server.Core.Model.ServerRequestModel;
import com.ethereal.server.Server.Abstract.Token;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public abstract class WebSocketToken extends Token {
    protected ChannelHandlerContext ctx;
    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void sendClientResponse(ClientResponseModel response) {
        if(ctx.channel() !=null && ctx.channel().isActive()){
            String json = config.getClientResponseModelSerialize().Serialize(response);
            //多转一次格式，用户可能使用非Config的编码.
            json = new String(json.getBytes(config.getCharset()));
            ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
        }
        else ctx.close();
    }

    @Override
    public void sendServerRequest(ServerRequestModel request) {
        if(ctx.channel() !=null && ctx.channel().isActive()){
            String json = config.getServerRequestModelSerialize().Serialize(request);
            //多转一次格式，用户可能使用非Config的编码.
            json = new String(json.getBytes(config.getCharset()));
            ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
        }
        else ctx.close();
    }

    @Override
    public void disConnect(String reason) {
        ctx.close();
        ctx = null;
    }
}
