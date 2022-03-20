package com.qins.net.core.boot;

import com.google.gson.Gson;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaClassLoader;
import com.qins.net.node.core.Server;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ApplicationContext {
    private ApplicationConfig config;
    private MetaClassLoader metaClassLoader;
    private Server server;
    private HashMap<String,String> nodes;
}
