package com.ethereal.meta.core.boot;

import com.ethereal.meta.net.core.NetConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerConfig extends ApplicationConfig{
    private int maxBufferSize = 10240;
    private int threadCount = 5;
    private int port = 80;
}
