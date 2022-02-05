package com.ethereal.meta.net.network.http.client;

import com.ethereal.meta.meta.Meta;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URI;
import java.util.concurrent.ExecutorService;

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
public class Http2Client {
    protected ExecutorService es;
    protected Meta meta;
    public Http2Client(Meta meta) {
        this.meta = meta;
    }
    public void connect() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            URI uri = new URI(meta.getNet().getNetConfig().getHost());
            CustomHandler webSocketHandler = new CustomHandler(es, meta);
            Bootstrap bootstrap = new Bootstrap();               //1
            bootstrap.group(group)                                //2
                    .channel(NioSocketChannel.class)            //3
                    .handler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch) {
                            //数据处理
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(meta.getNet().getNetConfig().getMaxBufferSize()));
                            ch.pipeline().addLast(webSocketHandler);
                            //心跳包
                            ch.pipeline().addLast(new IdleStateHandler(0,0,5));

                        }
                    });
            if(meta.getNet().getNetConfig().isSyncConnect()){
                bootstrap.connect(uri.getHost(), uri.getPort()).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            meta.getNet().onConnectFail();
                        }
                    }
                }).sync().channel();
            }
            else {
                bootstrap.connect(uri.getHost(), uri.getPort()).addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        meta.getNet().onConnectFail();
                    }
                }).channel();
            }
        }
        catch (Exception e){
            meta.onException(e);
        }
    }
}
