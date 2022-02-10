package com.ethereal.meta.core.boot;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationConfig {
    private int maxBufferSize = 10240;
    private int threadCount = 5;
    private int port = 80;
}
