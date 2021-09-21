package Old.NativeServer;

import java.util.List;

import Model.ClientResponseModel;
import Model.RPCException;
import Model.RPCLog;
import Model.ServerRequestModel;
import NativeServer.ServerConfig;
import NativeServer.ServerCore;
import RPCNet.Net;
import RPCNet.NetCore;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.javatuples.Pair;

public class CustomDecoder extends ByteToMessageDecoder {
    private int headSize = 32;//头包长度
    private int bodySize = 4;//数据大小长度
    private int patternSize = 1;//消息类型长度
    private int futureSize = 27;//后期看情况加
    private NativeServer.ServerConfig config;
    private String netName;
    private String serviceName;
    private Pair<String,String> clientKey;

    //下面这部分的byte用于接收数据
    private byte  pattern;
    private byte[] future = new byte[futureSize];
    private int dynamicAdjustBufferCount = -1;
    public CustomDecoder(String netName,String serviceName,Pair<String,String> clientKey, ServerConfig config){
        this.config = config;
        this.netName = netName;
        this.serviceName = serviceName;
        this.clientKey = clientKey;
    }
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.resetReaderIndex();
        while(in.readerIndex() < in.writerIndex()){
            int count = in.writerIndex() - in.readerIndex();

            if(headSize < count){
                int body_length = in.getIntLE(in.readerIndex());
                byte pattern = in.getByte(in.readerIndex() + bodySize);
                byte[] future = new byte[futureSize];
                in.getBytes(in.readerIndex()+ bodySize + patternSize,future,0, futureSize);
                int length = body_length + headSize;
                if(length <= count){
                    String data = null;
                    try{
                        data = in.toString(in.readerIndex() + headSize,body_length,config.getCharset());
                    }
                    catch(Exception e){
                        throw new RPCException(RPCException.ErrorCode.Runtime, String.format("%s-%s:用户数据错误，已自动断开连接!",
                                clientKey.getValue0() + ":" + clientKey.getValue1(),ctx.channel().remoteAddress()));
                    }

                    try{
                        Net net = NetCore.get(netName);
                        if(net == null){
                            throw new RPCException(RPCException.ErrorCode.Runtime,
                                    String.format("未找到net", netName));
                        }
                        if(pattern == 0){
                            ServerCore.get(netName,serviceName).onLog(RPCLog.LogCode.Runtime,"[服-请求]:" + data);
                            //服务器模型的反序列化 实体
                            ServerRequestModel serverRequestModel = config.getServerRequestModelDeserialize().Deserialize(data);
                            net.getServerRequestReceive().ServerRequestReceive(serverRequestModel);
                        }
                        else {
                            ClientCore.get(netName,serviceName).onLog(RPCLog.LogCode.Runtime,"[客-返回]:" + data);
                            ClientResponseModel clientResponseModel = config.getClientResponseModelDeserialize().Deserialize(data);
                            net.getClientResponseReceive().ClientResponseReceive(clientResponseModel);
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        ClientCore.get(netName,serviceName).onException(RPCException.ErrorCode.Runtime,e.getMessage());
                    }
                    in.readerIndex(in.readerIndex() + length);
                }
                else {
                    if(in.readerIndex() != 0){
                        in.setBytes(0,in,in.readerIndex(),count);
                        in.resetReaderIndex();
                        in.writerIndex(count);
                    }
                    if(length > in.capacity()){
                        if(length < in.maxCapacity()){
                            in.capacity(length);
                            dynamicAdjustBufferCount = config.getDynamicAdjustBufferCount();
                            return;
                        }
                        else {
                            ClientCore.get(netName,serviceName).onException(new RPCException(RPCException.ErrorCode.Runtime, String.format("%s-%s:用户请求数据量太大，中止接收！",
                                    netName + ":" + ctx.channel().remoteAddress())));
                        }
                    }
                    return;
                }
            }
            else {
                if(in.readerIndex()!=0){
                    in.setBytes(0,in,in.readerIndex(),count);
                    in.resetReaderIndex();
                    in.writerIndex(count);
                }
                return;
            }
        }
        in.resetReaderIndex();
        in.resetWriterIndex();
        if(in.capacity() > config.getBufferSize() && dynamicAdjustBufferCount-- == 0){
            in.capacity(config.getBufferSize());
            dynamicAdjustBufferCount = 1;
        }
    }
}
