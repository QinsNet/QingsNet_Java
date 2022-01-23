package com.ethereal.net.node.core;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeConfig {
    public int maxBufferSize = 10240;
    public int threadCount = 5;
    public String prefixes;
    public int port;
}
