package com.ethereal.meta.net.network.p2p.client;

import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.INetwork;
import com.ethereal.meta.util.SerializeUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.SocketAddress;
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
public class P2PClient implements INetwork {
    private final Meta meta;
    private final SocketAddress remote;
    private final SocketAddress local;
    private Channel channel;

    public P2PClient(Meta meta, SocketAddress remote, SocketAddress local) {
        this.meta = meta;
        this.remote = remote;
        this.local = local;
    }

    @Override
    public boolean start() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap();               //1
            bootstrap.group(group)                                //2
                    .channel(NioSocketChannel.class)            //3
                    .handler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch) {
                            //数据处理
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(meta.getNet().getNetConfig().getMaxBufferSize()));
                            //心跳包
                            ch.pipeline().addLast(new IdleStateHandler(0,0,5));
                            ch.pipeline().addLast(new P2PClientHandler(meta));
                        }
                    });
            if(meta.getNet().getNetConfig().isSyncConnect()){
                channel = bootstrap.connect(remote,local).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {

                        }
                    }
                }).sync().channel();
                return channel.isActive();
            }
            else {
                bootstrap.connect(remote,local).addListener((ChannelFutureListener) future -> {
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
        channel.writeAndFlush(res);
        channel.close();
    }
    @Override
    public boolean send(Object data) {
        if(data == null){
            return true;
        }
        else if(data instanceof ResponseMeta){
            ResponseMeta responseMeta = (ResponseMeta) data;
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,responseMeta.getMapping(), Unpooled.copiedBuffer(SerializeUtil.gson.toJson(responseMeta.getResult()).getBytes(StandardCharsets.UTF_8)));
            request.headers().set("id",responseMeta.getId());
            request.headers().set("error", SerializeUtil.gson.toJson(responseMeta.getError()));
            request.headers().set("protocol",responseMeta.getProtocol());
            request.headers().set("meta",responseMeta.getMeta());
            request.headers().set("value",responseMeta.getMapping());
            send(request);
        }
        else if(data instanceof RequestMeta){
            RequestMeta requestMeta = (RequestMeta) data;
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.POST,requestMeta.getMapping(),Unpooled.copiedBuffer(SerializeUtil.gson.toJson(requestMeta.getParams()).getBytes(StandardCharsets.UTF_8)));
            request.headers().set("id", requestMeta.getId());
            request.headers().set("protocol", requestMeta.getProtocol());
            request.headers().set("meta", requestMeta.getMeta());
            request.headers().set("value", requestMeta.getMapping());
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
        return false;
    }
}
