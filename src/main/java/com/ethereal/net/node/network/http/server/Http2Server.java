package com.ethereal.net.node.network.http.server;

import com.ethereal.net.node.core.NodeConfig;
import com.ethereal.net.node.network.INetwork;
import com.ethereal.net.service.core.Service;
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
    protected Service service;
    private Channel channel;
    public Http2Server(Service service) {
        this.service = service;
    }

    @Override
    public boolean start() {
        this.es = Executors.newFixedThreadPool(service.getNode().getConfig().threadCount);
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
                            ch.pipeline().addLast(new HttpObjectAggregator(service.getNode().getConfig().getMaxBufferSize()));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new CustomWebSocketHandler(es,service));
                        }
                    });
            channel = bootstrap.bind(service.getNode().getConfig().getPort()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()){
                        service.getNode().onStart();
                    }
                    else {
                        service.getNode().onClose();
                    }
                }
            }).channel();
            channel.closeFuture().sync();

        }
        catch (Exception exception){
            service.onException(exception);
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
