package com.ethereal.net.node.network.http.server;

import com.ethereal.net.net.config.NodeConfig;
import com.ethereal.net.net.core.Net;
import com.ethereal.net.node.network.INetwork;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebSocketServer implements INetwork {
    protected boolean isClose = false;
    protected ExecutorService es;
    protected NodeConfig config;
    protected String prefixes;
    private Channel channel;

    public WebSocketServer(NodeConfig config) {
        this.config = config;
        this.es = Executors.newFixedThreadPool(this.config.threadCount);
    }

    @Override
    public boolean start() {
        try {
            NioEventLoopGroup boss=new NioEventLoopGroup();
            NioEventLoopGroup work=new NioEventLoopGroup();
            try {
                URI uri = new URI(prefixes);
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(boss,work)                                //2
                        .channel(NioServerSocketChannel.class)            //3
                        .childHandler(new ChannelInitializer<SocketChannel>() {    //5
                            @Override
                            public void initChannel(SocketChannel ch) {
                                //数据处理
                                ch.pipeline().addLast(new HttpServerCodec());
                                ch.pipeline().addLast(new HttpObjectAggregator(config.getMaxBufferSize()));
                                ch.pipeline().addLast(new ChunkedWriteHandler());
                                ch.pipeline().addLast(new CustomWebSocketHandler(es));
                            }
                        });
                channel = bootstrap.bind(Net.getConfig().getNode().getPort()).channel();
                channel.closeFuture().sync();
            }
            catch (Exception e){
                Net.onException(e);
            }
            finally {
                boss.shutdownGracefully();
                work.shutdownGracefully();
            }
        } catch (Exception e){
            Net.onException(e);
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
