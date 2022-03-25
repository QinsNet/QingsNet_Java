package com.qins.net.node.http.recevier;

import com.qins.net.core.boot.ApplicationConfig;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.node.core.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class Receiver extends Server {
    protected ExecutorService es;
    protected HashMap<String,MetaClass> metas;
    protected ApplicationConfig config;
    EventLoopGroup boss = new NioEventLoopGroup();
    EventLoopGroup work = new NioEventLoopGroup();
    @Getter
    protected Channel channel;
    public Receiver(ApplicationConfig config, NodeAddress local, HashMap<String, MetaClass> metas) {
        this.config = config;
        this.local = local;
        this.metas = metas;
    }
    @Override
    public boolean start(){
        this.es = Executors.newFixedThreadPool(config.getThreadCount());
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,work)                                //2
                    .channel(NioServerSocketChannel.class)            //3
                    .childHandler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
                            //Http
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(config.getMaxBufferSize()));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            //Service
                            ch.pipeline().addLast(new ServiceHandler(es, metas,local));
                        }
                    });
            channel = bootstrap.bind(local.getHost(),local.getPort()).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()){
                    log.debug(String.format("%s-%s 服务器部署成功", local.getHost(),local.getPort()));
                }
                else {
                    log.debug(String.format("%s-%s 服务器部署失败", local.getHost(),local.getPort()));
                }
            }).channel();
            if(config.isServerSync()){
                channel.closeFuture().sync();
            }
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
