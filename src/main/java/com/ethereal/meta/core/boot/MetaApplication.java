package com.ethereal.meta.core.boot;

import com.ethereal.meta.core.entity.NodeAddress;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.util.MetaUtil;
import com.ethereal.meta.node.http.recevier.Receiver;
import com.ethereal.meta.request.core.Request;
import com.ethereal.meta.request.core.RequestInterceptor;
import com.ethereal.meta.util.SerializeUtil;
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
        return create(interceptor.getRequest().getMeta(),mapping,interceptor.getLocal(),interceptor.getRemote());
    }

    public static <T> T create(Meta root,String mapping,NodeAddress local,NodeAddress remote){
        LinkedList<String> mappings = new LinkedList<>(Arrays.asList(mapping.split("/")));
        mappings.removeFirst();
        Meta meta = MetaUtil.findMeta(root,mappings);
        if(meta == null){
            root.onException(new TrackException(TrackException.ExceptionCode.NotFoundMeta, String.format("%s%s 未找到", root.getPrefixes(),mapping)));
            return null;
        }
        return meta.newInstance(null,local,remote);
    }

    public static MetaApplication run(Class<?> instanceClass,String path){
        MetaApplication application = new MetaApplication();
        ApplicationContext context = new ApplicationContext();
        application.context = context;
        Meta root = Meta.newMeta(null,"", instanceClass);
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
