package com.ethereal.meta.net.core;


import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class NetConfig {
    private String host;
    private int maxBufferSize = 10240;
    private int threadCount = 5;
    private boolean syncConnect = true;
    private boolean Independence = true;
}
