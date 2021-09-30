package com.ethereal.server.Server.WebSocket;

import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Server.Abstract.Server;
import com.ethereal.server.Server.Delegate.CreateInstanceDelegate;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebSocketServer extends Server {
    protected boolean isClose = false;
    protected ExecutorService es;
    private Channel channel;
    public WebSocketServer(List<String> prefixes, CreateInstanceDelegate createMethod) {
        super(prefixes,createMethod);
        config = new WebSocketServerConfig();
        this.es= Executors.newFixedThreadPool(getConfig().threadCount);
    }

    public ExecutorService getEs() {
        return es;
    }

    public void setEs(ExecutorService es) {
        this.es = es;
    }


    public WebSocketServerConfig getConfig() {
        return (WebSocketServerConfig)config;
    }

    @Override
    public void Start() {
        NioEventLoopGroup boss=new NioEventLoopGroup();
        NioEventLoopGroup work=new NioEventLoopGroup();
        try {
            URI uri = new URI("ws://" + prefixes.get(0));
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(uri.toString(),null, false);
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss,work)                                //2
                    .channel(NioServerSocketChannel.class)            //3
                    .childHandler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch) {
                            //数据处理
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(getConfig().getMaxBufferSize()));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new CustomWebSocketHandler((WebSocketBaseToken) createMethod.createInstance(),netName,config,es, wsFactory));
                        }
                    });
            channel = bootstrap.bind(uri.getPort()).sync().channel();
            onListenerSuccess();
            channel.closeFuture().sync();
        }
        catch (Exception e){
            onException(new TrackException(e));
            onListenerFailEvent();
        }
        finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

    @Override
    public void Close() {
        if(!isClose){
            channel.close();
        }
    }
}
