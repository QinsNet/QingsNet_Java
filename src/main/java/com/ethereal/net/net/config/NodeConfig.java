package com.ethereal.net.net.config;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeConfig {
    public int maxBufferSize = 10240;
    public int threadCount = 5;
    public int port = 80;
}
