package com.ethereal.meta.node.p2p.sender;

import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.request.core.RequestContext;
import com.ethereal.meta.util.SerializeUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.charset.StandardCharsets;

/**
 * @ProjectName: YiXian
 * @Package: com.xianyu.yixian.com.ethereal.client.Core.Model
 * @ClassName: EchoClient
 * @Description: TCP客户端
 * @Author: Jianxian
 * @CreateDate: 2020/11/16 20:17
 * @UpdateUser: Jianxian
 * @UpdateDate: 2020/11/16 20:17
 * @UpdateRemark: 类的第一次生成
 * @Version: 1.0
 */
public class Sender extends com.ethereal.meta.node.core.Node {

    private Channel channel;
    public Sender(Meta meta, RequestContext context) {
        super(meta,context);
    }

    @Override
    public boolean start() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap();               //1
            bootstrap.group(group)                                //2
                    .channel(NioSocketChannel.class)            //3
                    .option(ChannelOption.SO_REUSEADDR,true)
                    .handler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch) {
                            //编解码
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new StringDecoder());
                            //Http
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(nodeConfig.getMaxBufferSize()));
                            //Request
                            ch.pipeline().addLast(new IdleStateHandler(0,0,5));
                            ch.pipeline().addLast(new RequestHandler(meta,context));
                        }
                    });
            if(nodeConfig.isSyncConnect()){
                channel = bootstrap.connect(context.getRemote().getHost(),Integer.parseInt(context.getRemote().getPort())).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            Console.debug(String.format("%s-%s 连接成功", context.getRemote().getHost(),context.getRemote().getPort()));
                        }
                    }
                }).sync().channel();
                return channel.isActive();
            }
            else {
                 channel = bootstrap.bind().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {

                    }
                }).channel();
                return channel.isActive();
            }
        }
        catch (Exception e){
            meta.onException(e);
        }
        return false;
    }

    private void send(DefaultFullHttpRequest res) {
        start();
        channel.writeAndFlush(res);
    }
    @Override
    public boolean send(Object data) {

        if(data == null){
            return true;
        }
        else if(data instanceof RequestMeta){
            RequestMeta requestMeta = (RequestMeta) data;
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.POST,requestMeta.getMapping(),Unpooled.copiedBuffer(SerializeUtil.gson.toJson(requestMeta.getParams()).getBytes(StandardCharsets.UTF_8)));
            request.headers().set("protocol", requestMeta.getProtocol());
            request.headers().set("instance", requestMeta.getMeta());
            request.headers().set("host", requestMeta.getHost());
            request.headers().set("port", requestMeta.getPort());
            send(request);
        }
        else if(data instanceof byte[]){
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((byte[]) data)));
        }
        else if(data instanceof DefaultFullHttpResponse){
            send(data);
        }
        else if(data instanceof String){
            send(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK, Unpooled.copiedBuffer((String) data,StandardCharsets.UTF_8)));
        }
        return true;
    }

    @Override
    public boolean close() {
        try {
            if(channel != null){
                channel.close().sync();
            }
            return true;
        } catch (InterruptedException e) {
            meta.onException(e);
            return false;
        }
    }
}
