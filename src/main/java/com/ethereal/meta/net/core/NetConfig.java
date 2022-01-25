package com.ethereal.meta.net.core;


import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class NetConfig {
    private int maxBufferSize = 10240;
    private int threadCount = 5;
    protected Charset charset = StandardCharsets.UTF_8;
}
