package com.ethereal.meta.node.core;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeConfig {
    private String host;
    private int maxBufferSize = 10240;
    private int threadCount = 5;
    private boolean syncConnect = true;
    private boolean Independence = true;
}
