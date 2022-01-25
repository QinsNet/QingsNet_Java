package com.ethereal.meta.net.network;

import com.ethereal.meta.net.core.NetConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerConfig extends NetConfig {
    private int port;
}
