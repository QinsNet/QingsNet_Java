package com.ethereal.meta.node.core;


import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class NodeConfig {
    private int maxBufferSize = 10240;
    private int threadCount = 5;
    private String prefixes;
    private int port;
    protected Charset charset = StandardCharsets.UTF_8;
}
