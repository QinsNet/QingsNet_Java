package com.ethereal.meta.node.network.http.server;

import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.node.network.INetwork;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Http2Server implements INetwork {
    protected boolean isClose = false;
    protected ExecutorService es;
    protected Meta meta;
    private Channel channel;
    public Http2Server(Meta meta) {
        this.meta = meta;
    }

    @Override
    public boolean start() {
        this.es = Executors.newFixedThreadPool(meta.getNodeConfig().getThreadCount());
        NioEventLoopGroup boss=new NioEventLoopGroup();
        NioEventLoopGroup work=new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,work)                                //2
                    .channel(NioServerSocketChannel.class)            //3
                    .childHandler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch) {
                            //数据处理
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(meta.getNodeConfig().getMaxBufferSize()));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new CustomWebSocketHandler(es, meta));
                        }
                    });
            channel = bootstrap.bind(serviceNet.getNode().getConfig().getPort()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()){
                        serviceNet.getNode().onStart();
                    }
                    else {
                        serviceNet.getNode().onClose();
                    }
                }
            }).channel();
            channel.closeFuture().sync();

        }
        catch (Exception exception){
            serviceNet.onException(exception);
        }
        finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
        return true;
    }

    @Override
    public boolean send(Object data) {
        return true;
    }

    @Override
    public boolean close() {
        if(!isClose && channel.isActive()){
            channel.close();
            isClose = true;
        }
        return true;
    }
}
