package com.ethereal.meta.node.p2p.recevier;

import com.ethereal.meta.core.boot.ApplicationConfig;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.node.core.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Receiver extends Server {
    protected ExecutorService es;
    protected Meta root;
    protected ApplicationConfig config;
    EventLoopGroup boss = new NioEventLoopGroup();
    EventLoopGroup work = new NioEventLoopGroup();
    @Getter
    protected Channel channel;
    public Receiver(ApplicationConfig config, Meta root) {
        this.config = config;
        this.root = root;
    }
    @Override
    public boolean start(){
        this.es = Executors.newFixedThreadPool(config.getThreadCount());
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,work)                                //2
                    .channel(NioServerSocketChannel.class)            //3
                    .option(ChannelOption.SO_REUSEADDR,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
                            //Http
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(config.getMaxBufferSize()));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            //Service
                            ch.pipeline().addLast(new ServiceHandler(es, root));
                        }
                    });
            channel = bootstrap.bind(config.getPort()).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()){

                }
                else {

                }
            }).channel();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean close() {
        if(channel.isActive()){
            boss.shutdownGracefully();
        }
        return true;
    }
}
