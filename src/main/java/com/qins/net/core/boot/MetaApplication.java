package com.qins.net.core.boot;

import com.qins.net.core.console.Console;
import com.qins.net.core.entity.TrackException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.meta.core.MetaClassLoader;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.node.http.recevier.Receiver;
import com.qins.net.node.util.NodeUtil;
import com.qins.net.request.cglib.RequestInterceptor;
import com.qins.net.util.SerializeUtil;
import lombok.Getter;
import lombok.NonNull;
import net.sf.cglib.proxy.Factory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MetaApplication {
    @Getter
    private ApplicationContext context;

    public <T> T create(Class<?> instanceClass) throws NewInstanceException {
        try {
            Meta meta = instanceClass.getAnnotation(Meta.class);
            if(meta == null)throw new LoadClassException(String.format("%s 未定义@Meta", instanceClass.getName()));
            return context.getMetaClassLoader().loadMetaClass(meta,instanceClass).newInstance(new HashMap<>());
        }
        catch (LoadClassException e) {
            throw new NewInstanceException(e);
        }
    }

    public @NonNull static <T> T create(Object instance, Class<?> instanceClass) throws NewInstanceException {
        try {
            Meta meta = instanceClass.getAnnotation(Meta.class);
            if(meta == null)throw new LoadClassException(String.format("%s 未定义@Meta", instanceClass.getName()));
            RequestInterceptor interceptor = (RequestInterceptor) ((Factory)(instance)).getCallback(1);
            MetaClassLoader classLoader = (MetaClassLoader) Thread.currentThread().getContextClassLoader();
            return classLoader.loadMetaClass(meta,instanceClass).newInstance(new HashMap<>());
        }
        catch (LoadClassException e) {
            throw new NewInstanceException(e);
        }
    }

    public static MetaApplication run(String path){
        MetaApplication application = new MetaApplication();
        ApplicationContext context = new ApplicationContext();
        application.context = context;
        context.setMetaClassLoader(new MetaClassLoader());
        Thread.currentThread().setContextClassLoader(context.getMetaClassLoader());
        context.setConfig(application.loadConfig(path));
        context.setServer(new Receiver(context.getConfig(),new NodeAddress("localhost",context.getConfig().getPort()), context.getMetaClassLoader()));
        context.getServer().start();
        return application;
    }

    public MetaApplication publish(Class<?> instanceClass) throws LoadClassException {
        Meta meta = instanceClass.getAnnotation(Meta.class);
        if(meta == null)throw new LoadClassException(String.format("%s 未定义@Meta", instanceClass.getName()));
        context.getMetaClassLoader().loadMetaClass(meta,instanceClass);
        return this;
    }

    public MetaApplication defineNode(Object instance, String name, String address) throws TrackException {
        if(!NodeUtil.defineNode(instance, name, address)){
            throw new TrackException(TrackException.ExceptionCode.Runtime, String.format("[%s] 定义节点 %s:%s 失败", instance.getClass().getName(), name, address));
        }
        return this;
    }
    public MetaApplication defineNode(String name, String address) {
        HashMap<String,String> nodes = ((MetaClassLoader)Thread.currentThread().getContextClassLoader()).getNodes();
        nodes.remove(name);
        nodes.put(name,address);
        return this;
    }

    private ApplicationConfig loadConfig(String path){
        ApplicationConfig config = null;
        File file = new File(path);
        if(file.exists()){
            try {
                config = SerializeUtil.yaml.load(new FileInputStream(file));
            }
            catch (FileNotFoundException e) {
                Console.error(e.getMessage());
                e.printStackTrace();
            }
        }
        if(config == null){
            config = new ApplicationConfig();
            try {
                String data = SerializeUtil.yaml.dump(config);
                new FileOutputStream(file).write(data.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                Console.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return config;
    }
}
