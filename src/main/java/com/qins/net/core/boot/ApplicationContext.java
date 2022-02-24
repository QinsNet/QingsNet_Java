package com.qins.net.core.boot;

import com.qins.net.meta.core.MetaNodeField;
import com.qins.net.node.core.Server;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationContext {
    private ApplicationConfig config;
    private MetaNodeField root;
    private Server server;
}
