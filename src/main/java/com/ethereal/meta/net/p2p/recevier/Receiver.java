package com.ethereal.meta.net.p2p.recevier;

import com.ethereal.meta.core.boot.ApplicationConfig;
import com.ethereal.meta.meta.Meta;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Receiver {
    protected ExecutorService es;
    protected Meta root;
    protected ApplicationConfig config;
    protected Channel channel;
    public Receiver(ApplicationConfig config, Meta root) {
        this.config = config;
        this.root = root;
    }

    public boolean start(){
        this.es = Executors.newFixedThreadPool(config.getThreadCount());
        NioEventLoopGroup boss=new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss)                                //2
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
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
        }
        return true;
    }
}
