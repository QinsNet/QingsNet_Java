package com.ethereal.meta.core.boot;

import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.root.Root;
import com.ethereal.meta.meta.root.RootMeta;
import com.ethereal.meta.net.network.IServer;
import com.ethereal.meta.net.network.http.server.Http2Server;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationContext {
    private ApplicationConfig config;
    private RootMeta root = new RootMeta();
    private IServer server;
}
