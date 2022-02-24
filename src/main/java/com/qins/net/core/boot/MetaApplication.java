package com.qins.net.core.boot;

import com.qins.net.core.entity.TrackException;
import com.qins.net.meta.core.MetaNodeField;
import com.qins.net.meta.util.MetaUtil;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.node.http.recevier.Receiver;
import com.qins.net.request.core.RequestInterceptor;
import com.qins.net.component.StandardMetaNodeField;
import com.qins.net.util.SerializeUtil;
import lombok.Getter;
import net.sf.cglib.proxy.Factory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class MetaApplication {
    @Getter
    private ApplicationContext context;

    public <T> T create(String mapping, NodeAddress address){
        return create(context.getRoot(),mapping,context.getServer().getLocal(),address);
    }

    public static <T> T create(Object instance,String mapping){
        Factory factory = (Factory) instance;
        RequestInterceptor interceptor = (RequestInterceptor) factory.getCallback(1);
        return create(interceptor.getRequest().getMetaNodeField(),mapping,interceptor.getLocal(),interceptor.getRemote());
    }

    public static <T> T create(MetaNodeField root, String mapping, NodeAddress local, NodeAddress remote){
        LinkedList<String> mappings = new LinkedList<>(Arrays.asList(mapping.split("/")));
        mappings.removeFirst();
        MetaNodeField metaNodeField = MetaUtil.findMeta(root,mappings);
        if(metaNodeField == null){
            root.onException(new TrackException(TrackException.ExceptionCode.NotFoundMeta, String.format("%s%s 未找到", root.getMapping(),mapping)));
            return null;
        }
        return metaNodeField.newInstance(null,local,remote);
    }

    public static MetaApplication run(Class<?> instanceClass,String path){
        MetaApplication application = new MetaApplication();
        ApplicationContext context = new ApplicationContext();
        application.context = context;
        MetaNodeField root = new StandardMetaNodeField(instanceClass);
        context.setRoot(root);
        context.setConfig(application.loadConfig(path));
        context.setServer(new Receiver(context.getConfig(),new NodeAddress("localhost",context.getConfig().getPort()), root));
        context.getServer().start();
        return application;
    }

    private ApplicationConfig loadConfig(String path){
        ApplicationConfig config = null;
        File file = new File(path);
        if(file.exists()){
            try {
                config = SerializeUtil.yaml.load(new FileInputStream(file));
            }
            catch (FileNotFoundException e) {
                context.getRoot().onException(e);
            }
        }
        if(config == null){
            config = new ApplicationConfig();
            try {
                String data = SerializeUtil.yaml.dump(config);
                new FileOutputStream(file).write(data.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                context.getRoot().onException(e);
            }
        }
        return config;
    }
}
