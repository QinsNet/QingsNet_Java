package com.ethereal.meta.core.boot;

import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.net.network.http.server.Http2Server;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class ApplicationContext {
    private ApplicationConfig config;
    private HashMap<String, Class<?>> root = new HashMap<>();
}
