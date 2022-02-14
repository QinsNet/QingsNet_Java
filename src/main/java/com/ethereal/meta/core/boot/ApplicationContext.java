package com.ethereal.meta.core.boot;

import com.ethereal.meta.meta.Meta;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationContext {
    private ApplicationConfig config;
    private Meta root;
    private IServer server;
}
