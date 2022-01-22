package com.ethereal.net.node.network.http.server;
import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.core.entity.ResponseMeta;
import com.ethereal.net.core.entity.Error;
import com.ethereal.net.node.core.Node;
import com.ethereal.net.node.network.INetwork;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

public class CustomWebSocketHandler extends SimpleChannelInboundHandler<FullHttpRequest>  implements INetwork{
    protected ExecutorService es;
    protected Node node;
    protected ChannelHandlerContext ctx;
    public CustomWebSocketHandler(ExecutorService executorService){
        this.es = executorService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if(node != null){
            node.onDisConnect();
            node.setNetwork(null);
            node = null;
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        RequestMeta requestMeta;
        if(req.method() == HttpMethod.GET){
            requestMeta = new RequestMeta();
        }
        else if(req.method() == HttpMethod.POST){
            requestMeta =
        }
        else {
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((String.format("%s请求不支持", req.method())),StandardCharsets.UTF_8)));
        }
        requestMeta.setMapping(req.uri());
        requestMeta.setParams();
        if(service == null){
            sendHttpToClient(ctx,new ResponseMeta(null,null,new Error(Error.ErrorCode.NotFoundNet,   "未找到Service:"+ service_name,null)));
            return;
        }
        token =(WebSocketToken)service.getCreateMethod().createInstance();
        token.setService(service);
        token.setCtx(ctx);
        //要求Upgrade为websocket，过滤掉get/Post
        if (!(("websocket".equals(req.headers().get("Upgrade"))) || ("WebSocket".equals(req.headers().get("Upgrade"))))) {
            try {
                String data = req.content().toString(token.getService().getConfig().getCharset());
                RequestMeta requestMeta = token.getService().getConfig().getClientRequestModelDeserialize().Deserialize(data);
                token.setCanRequest(false);
                token.onConnect();
                ResponseMeta responseMetaModel = null;
                responseMetaModel = es.submit(() -> token.getService().clientRequestReceiveProcess(token, requestMeta)).get();
                if(responseMetaModel !=null)sendHttpToClient(ctx, responseMetaModel);
                return;
            }
            catch (Exception e){
                sendHttpToClient(ctx,new ResponseMeta(null,null,new Error(Error.ErrorCode.NotFoundNet, "未找到节点{net}")));
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


    @Override
    public boolean start() {
        return true;
    }

    private void send(DefaultFullHttpResponse res) {
        ctx.channel().writeAndFlush(res);
        ctx.close();
    }

    @Override
    public boolean send(Object data) {
        if(data instanceof byte[]){
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
        return false;
    }
}
