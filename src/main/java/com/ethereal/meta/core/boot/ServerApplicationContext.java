package com.ethereal.meta.core.boot;


import com.ethereal.meta.net.network.http.server.Http2Server;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerApplicationContext extends ApplicationContext{
    Http2Server server;
}
