package com.ethereal.meta.net.network.http.server;

import com.ethereal.meta.core.boot.ApplicationConfig;
import com.ethereal.meta.meta.root.RootMeta;
import com.ethereal.meta.net.network.IServer;
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
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Http2Server implements IServer {
    protected ExecutorService es;
    protected RootMeta root;
    protected ApplicationConfig config;
    @Getter
    protected Channel channel;
    NioEventLoopGroup boss=new NioEventLoopGroup();
    NioEventLoopGroup work=new NioEventLoopGroup();
    public Http2Server(ApplicationConfig config, RootMeta root) {
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
                    .childHandler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
                            //数据处理
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(config.getMaxBufferSize()));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new CustomHandler(es, root));
                        }
                    });
            channel = bootstrap.bind(config.getPort()).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()){

                }
                else {

                }
            }).sync().channel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean close() {
        if(!channel.isActive()){
            boss.shutdownGracefully();
            work.shutdownGracefully();
            channel.close();
        }
        return true;
    }
}
