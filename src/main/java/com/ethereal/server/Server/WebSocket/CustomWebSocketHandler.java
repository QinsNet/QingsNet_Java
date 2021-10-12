package com.ethereal.server.Server.WebSocket;
import com.ethereal.server.Core.Model.ClientRequestModel;
import com.ethereal.server.Core.Model.ClientResponseModel;
import com.ethereal.server.Core.Model.Error;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Net.Abstract.Net;
import com.ethereal.server.Net.NetCore;
import com.ethereal.server.Server.Abstract.ServerConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CustomWebSocketHandler extends SimpleChannelInboundHandler<Object> {
    protected WebSocketServerHandshaker handshaker;
    protected WebSocketToken token;
    protected WebSocketServerHandshakerFactory handshakerFactory;
    protected ExecutorService es;
    public CustomWebSocketHandler(WebSocketToken token, String netName, ServerConfig config, ExecutorService executorService, WebSocketServerHandshakerFactory handshakerFactory){
        this.handshakerFactory = handshakerFactory;
        this.token = token;
        this.token.setConfig(config);
        token.setNetName(netName);
        this.es = executorService;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest){
            //以http请求形式接入，但是走的是websocket
            handleHttpRequest(ctx,(FullHttpRequest) msg);
        }
        else if (msg instanceof  WebSocketFrame){
            //处理websocket客户端的消息
            handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        token.setCtx(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        token.onDisconnectEvent();
        token.setCtx(null);
        token = null;
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame){
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }
        try {
            String data = frame.content().toString(token.getConfig().getCharset());
            ClientRequestModel clientRequestModel = token.getConfig().getClientRequestModelDeserialize().Deserialize(data);
            Net net = NetCore.get(token.getNetName());
            if(net == null){
                token.sendClientResponse(new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundNet,String.format("未找到net%s", token.getNetName())),clientRequestModel.getId(),clientRequestModel.getService()));
                token.disConnect("未找到Net");
                return;
            }
            ClientResponseModel responseModel = null;
            responseModel = es.submit(() -> net.clientRequestReceiveProcess(token,clientRequestModel)).get(token.getConfig().getProcessTimeout(), TimeUnit.MILLISECONDS);
            if(responseModel == null)throw new TrackException(TrackException.ErrorCode.Runtime, String.format("%s-%s-执行超时", clientRequestModel.getMethodId(),clientRequestModel.getService()));
            token.sendClientResponse(responseModel);
        }
        catch (Exception e){
            token.sendClientResponse(new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundNet,String.format("%s", e.getMessage())),null,null));
        }
    }
    /**
     * 唯一的一次http请求，用于创建websocket
     * */
    private void handleHttpRequest(ChannelHandlerContext ctx,
                                   FullHttpRequest req) throws ExecutionException, InterruptedException, TimeoutException {
        //要求Upgrade为websocket，过滤掉get/Post
        if (!(("websocket".equals(req.headers().get("Upgrade"))) || ("WebSocket".equals(req.headers().get("Upgrade"))))) {
            try {
                String data = req.content().toString(token.getConfig().getCharset());
                ClientRequestModel clientRequestModel = token.getConfig().getClientRequestModelDeserialize().Deserialize(data);
                Net net = NetCore.get(token.getNetName());
                if(net == null){
                    sendHttpToClient(ctx,new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundNet, "未找到节点{netName}")
                            ,clientRequestModel.getId(),clientRequestModel.getService()));
                    return;
                }
                token.setCanRequest(false);
                token.onConnect();
                ClientResponseModel responseModel = null;
                responseModel = es.submit(() -> net.clientRequestReceiveProcess(token,clientRequestModel)).get(token.getConfig().getProcessTimeout(), TimeUnit.MILLISECONDS);
                sendHttpToClient(ctx,responseModel);
                return;
            }
            catch (Exception e){
                sendHttpToClient(ctx,new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundNet, "未找到节点{netName}")
                        ,null,null));
            }
            finally {
                ctx.close();
            }
        }

        handshaker = handshakerFactory.newHandshaker(req);
        if (handshaker == null) {
            System.out.println("空");
            WebSocketServerHandshakerFactory
                    .sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
            token.onConnect();
        }
    }
    /**
     * 拒绝不合法的请求，并返回错误信息
     * */
    private void sendHttpToClient(ChannelHandlerContext ctx, ClientResponseModel responseModel) {
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        res.setProtocolVersion(HttpVersion.HTTP_1_1);
        res.content().writeBytes(token.getConfig().getClientResponseModelSerialize().Serialize(responseModel).getBytes(token.getConfig().getCharset()));
        ctx.channel().writeAndFlush(res);
    }
}
