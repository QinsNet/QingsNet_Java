package com.ethereal.server.Server.WebSocket;
import com.ethereal.server.Core.Model.ClientRequestModel;
import com.ethereal.server.Core.Model.ClientResponseModel;
import com.ethereal.server.Core.Model.Error;
import com.ethereal.server.Service.Abstract.Service;
import com.ethereal.server.Service.ServiceCore;
import com.ethereal.server.Service.WebSocket.WebSocketToken;
import com.ethereal.server.Utils.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

public class CustomWebSocketHandler extends SimpleChannelInboundHandler<Object> {
    protected WebSocketServerHandshaker handshaker;
    protected WebSocketServerHandshakerFactory handshakerFactory;
    protected ExecutorService es;
    protected WebSocketToken token;
    protected String net_name;
    public CustomWebSocketHandler(String net_name,ExecutorService executorService, WebSocketServerHandshakerFactory handshakerFactory){
        this.net_name = net_name;
        this.handshakerFactory = handshakerFactory;
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

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(token != null){
            token.onDisconnect();
            token.setService(null);
            token.setCtx(null);
            token = null;
        }
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
            String data = frame.content().toString(token.getService().getConfig().getCharset());
            ClientRequestModel clientRequestModel = token.getService().getConfig().getClientRequestModelDeserialize().Deserialize(data);
            ClientResponseModel responseModel = null;
            responseModel = es.submit(() -> token.getService().clientRequestReceiveProcess(token,clientRequestModel)).get();
            if(responseModel != null)token.sendClientResponse(responseModel);
        }
        catch (Exception e){
            token.sendClientResponse(new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundNet,String.format("%s", e.getMessage()))));
        }
    }
    /**
     * 唯一的一次http请求，用于创建websocket
     * */
    private void handleHttpRequest(ChannelHandlerContext ctx,
                                   FullHttpRequest req) {
        String[] urls = req.uri().split("/");
        if(urls.length == 0){
            sendHttpToClient(ctx,new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundNet,   "URL路径有误:"+ req.uri())));
            return;
        }
        String service_name = null;
        if(urls[urls.length-1].equals("")){
            service_name = urls[urls.length-2];
        }
        else service_name = urls[urls.length-1];
        Service service = ServiceCore.get(net_name,service_name);
        if(service == null){
            sendHttpToClient(ctx,new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundNet,   "未找到Service:"+ service_name,null)));
            return;
        }
        token =(WebSocketToken)service.getCreateMethod().createInstance();
        token.setService(service);
        token.setCtx(ctx);
        //要求Upgrade为websocket，过滤掉get/Post
        if (!(("websocket".equals(req.headers().get("Upgrade"))) || ("WebSocket".equals(req.headers().get("Upgrade"))))) {
            try {
                String data = req.content().toString(token.getService().getConfig().getCharset());
                ClientRequestModel clientRequestModel = token.getService().getConfig().getClientRequestModelDeserialize().Deserialize(data);
                token.setCanRequest(false);
                token.onConnect();
                ClientResponseModel responseModel = null;
                responseModel = es.submit(() -> token.getService().clientRequestReceiveProcess(token,clientRequestModel)).get();
                if(responseModel!=null)sendHttpToClient(ctx,responseModel);
                return;
            }
            catch (Exception e){
                sendHttpToClient(ctx,new ClientResponseModel(null,null,new Error(Error.ErrorCode.NotFoundNet, "未找到节点{net}")));
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
    private void sendHttpToClient(ChannelHandlerContext ctx ,ClientResponseModel responseModel) {
        DefaultFullHttpResponse res;
        if(responseModel.getError()==null)res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        else res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
        if(token == null)res.content().writeBytes(Utils.gson.toJson(responseModel).getBytes(StandardCharsets.UTF_8));
        else res.content().writeBytes(token.getService().getConfig().getClientResponseModelSerialize().Serialize(responseModel).getBytes(token.getService().getConfig().getCharset()));
        ctx.channel().writeAndFlush(res);
        ctx.close();
    }
}
