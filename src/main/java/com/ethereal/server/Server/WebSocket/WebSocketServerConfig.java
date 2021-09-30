package com.ethereal.server.Server.WebSocket;

import com.ethereal.server.Server.Abstract.ServerConfig;

public class WebSocketServerConfig extends ServerConfig {
    protected int maxBufferSize = 10240;
    public int threadCount = 5;

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
