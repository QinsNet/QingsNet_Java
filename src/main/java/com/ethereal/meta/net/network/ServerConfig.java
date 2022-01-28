package com.ethereal.meta.net.network;

import com.ethereal.meta.net.core.NetConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerConfig {
    private int maxBufferSize = 10240;
    private int threadCount = 5;
    private int port;
}
