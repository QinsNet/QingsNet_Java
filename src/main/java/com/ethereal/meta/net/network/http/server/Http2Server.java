package com.ethereal.meta.net.network.http.server;

import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.Server;
import com.ethereal.meta.net.network.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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

public class Http2Server extends Server {
    protected ExecutorService es;
    protected Class<? extends Meta> rootMetaClass;
    protected ServerConfig config;
    private Channel channel;
    public Http2Server(Class<? extends Meta> rootMetaClass) {
        this.rootMetaClass = rootMetaClass;
    }
    @Override
    public boolean start(){
        this.es = Executors.newFixedThreadPool(config.getThreadCount());
        NioEventLoopGroup boss=new NioEventLoopGroup();
        NioEventLoopGroup work=new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,work)                                //2
                    .channel(NioServerSocketChannel.class)            //3
                    .childHandler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch) throws IllegalAccessException {
                            //数据处理
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(config.getMaxBufferSize()));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new CustomHandler(es, rootMetaClass));
                        }
                    });
            channel = bootstrap.bind(config.getPort()).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()){

                }
                else {

                }
            }).channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
        return true;
    }

    @Override
    public boolean close() {
        if(!channel.isActive()){
            channel.close();
        }
        return true;
    }
}
