package Old.NativeServer;

import NativeServer.Interface.CreateInstanceDelegate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ServerConfig {
    //对Netty的配置部分
    private int bufferSize = 1024;
    private int maxBufferSize = 10240;
    private Charset charset = StandardCharsets.UTF_8;
    private int dynamicAdjustBufferCount = 1;
    private boolean nettyAdaptBuffer = false;
    private CreateInstanceDelegate createInstance;

    public CreateInstanceDelegate getCreateInstance() {
        return createInstance;
    }

    public void setCreateInstance(CreateInstanceDelegate createInstance) {
        this.createInstance = createInstance;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public int getDynamicAdjustBufferCount() {
        return dynamicAdjustBufferCount;
    }

    public void setDynamicAdjustBufferCount(int dynamicAdjustBufferCount) {
        this.dynamicAdjustBufferCount = dynamicAdjustBufferCount;
    }

    public boolean isNettyAdaptBuffer() {
        return nettyAdaptBuffer;
    }

    public void setNettyAdaptBuffer(boolean nettyAdaptBuffer) {
        this.nettyAdaptBuffer = nettyAdaptBuffer;
    }

    public ServerConfig(CreateInstanceDelegate delegate){
        this.createInstance = delegate;
    }
}
