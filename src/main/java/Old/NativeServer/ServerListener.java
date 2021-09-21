package Old.NativeServer;

import Model.RPCException;
import Model.RPCLog;
import NativeServer.CustomDecoder;
import NativeServer.Event.ConnectFailEvent;
import NativeServer.Event.ConnectSuccessEvent;
import NativeServer.Event.ExceptionEvent;
import NativeServer.Event.LogEvent;
import NativeServer.ServerConfig;
import RPCNet.Net;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import org.javatuples.Pair;

import java.util.concurrent.TimeUnit;

public class ServerListener {
    //ip-port
    private Pair<String,String> serverKey;
    //网关名z
    private String netName;
    //服务名
    private String serviceName;
    //配置项
    private ServerConfig config;
    //服务器运行状态
    private volatile boolean isRunning = false;
    //处理Accept连接事件的线程，这里线程数设置为1即可，netty处理链接事件默认为单线程，过度设置反而浪费cpu资源
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    //处理hadnler的工作线程，其实也就是处理IO读写 。线程数据默认为 CPU 核心数乘以2
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ExceptionEvent exceptionEvent = new ExceptionEvent();
    private LogEvent logEvent = new LogEvent();
    private ConnectSuccessEvent connectSuccessEvent = new ConnectSuccessEvent();
    private ConnectFailEvent connectFailEvent = new ConnectFailEvent();

    public ExceptionEvent getExceptionEvent() {
        return exceptionEvent;
    }

    public void setExceptionEvent(ExceptionEvent exceptionEvent) {
        this.exceptionEvent = exceptionEvent;
    }

    public LogEvent getLogEvent() {
        return logEvent;
    }

    public void setLogEvent(LogEvent logEvent) {
        this.logEvent = logEvent;
    }

    public void start() throws Exception{
        //创建ServerBootstrap实例
        ServerBootstrap serverBootstrap=new ServerBootstrap();
        //初始化ServerBootstrap的线程组
        serverBootstrap.group(bossGroup,workerGroup);//
        //设置将要被实例化的ServerChannel类
        serverBootstrap.channel(NioServerSocketChannel.class);//
        //在ServerChannelInitializer中初始化ChannelPipeline责任链，并添加到serverBootstrap中
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                //IdleStateHandler心跳机制,如果超时触发Handle中userEventTrigger()方法
                pipeline.addLast("idleStateHandler",
                        new IdleStateHandler(15, 0, 0, TimeUnit.MINUTES));
                //解码器
                pipeline.addLast(new CustomDecoder(netName,serviceName,serverKey,config));
            }
        });
        //标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        if(config.isNettyAdaptBuffer()){
            serverBootstrap.option(ChannelOption.RCVBUF_ALLOCATOR,new AdaptiveRecvByteBufAllocator());
        }
        else serverBootstrap.option(ChannelOption.RCVBUF_ALLOCATOR,new FixedRecvByteBufAllocator(config.getBufferSize()));
        // 是否启用心跳保活机机制
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        //绑定端口后，开启监听
        ChannelFuture channelFuture = serverBootstrap.bind(Integer.parseInt(serverKey.getValue1())).sync();
        if(channelFuture.isSuccess()){
            System.out.println("TCP服务启动 成功---------------");
        }
    }

    /**
     * 服务启动
     */
    public synchronized void startServer() {
        try {
            this.start();
        }catch(Exception ex) {

        }
    }

    /**
     * 服务关闭
     */
    public synchronized void stopServer() throws Exception {
        if (!this.isRunning) {
            throw new IllegalStateException(this.getName() + " 未启动 .");
        }
        this.isRunning = false;
        try {
            Future<?> future = this.workerGroup.shutdownGracefully().await();
            if (!future.isSuccess()) {
                onException(RPCException.ErrorCode.Runtime,"workerGroup 无法正常停止:\n" + future.cause());
            }

            future = this.bossGroup.shutdownGracefully().await();
            if (!future.isSuccess()) {
                onException(RPCException.ErrorCode.Runtime,"bossGroup 无法正常停止:\n" + future.cause());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onLog(RPCLog.LogCode.Runtime,"TCP服务已经停止...");
    }

    private String getName() {
        return "TCP-Server";
    }

    public void onException(RPCException.ErrorCode code, String message) throws Exception {
        onException(new RPCException(code,message));
    }

    public void onException(Exception exception) throws Exception {
        exceptionEvent.onEvent(exception,this);
        throw exception;
    }

    public void onLog(RPCLog.LogCode code, String message){
        onLog(new RPCLog(code,message));
    }

    public void onLog(RPCLog log){
        logEvent.onEvent(log,this);
    }

    public void onConnectSuccess()  {
        connectSuccessEvent.onEvent(this);
    }
    public void onConnectFailEvent() throws Exception {
        connectFailEvent.onEvent(this);
    }

    public ServerListener(Net net, Pair<String ,String > serverKey, ServerConfig config){


    }
}
