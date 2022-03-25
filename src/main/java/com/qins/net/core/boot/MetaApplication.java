package com.qins.net.core.boot;

import com.qins.net.core.exception.TrackException;
import com.qins.net.core.exception.LoadClassException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.meta.annotation.instance.MetaPact;
import com.qins.net.meta.core.MetaClass;
import com.qins.net.meta.core.MetaClassLoader;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.meta.util.PackageScanner;
import com.qins.net.node.http.recevier.Receiver;
import com.qins.net.node.util.NodeUtil;
import com.qins.net.util.AnnotationUtil;
import com.qins.net.util.SerializeUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

@Log4j2
public class MetaApplication {
    @Getter
    private static ApplicationContext context;
    public @NonNull static <T> T create(Class<?> instanceClass) throws NewInstanceException {
        try {
            MetaPact pact = AnnotationUtil.getMetaPact(instanceClass);
            if(pact == null)throw new LoadClassException(String.format("%s 未定义@Meta", instanceClass.getName()));
            MetaClassLoader classLoader = context.getMetaClassLoader();
            MetaClass metaClass = (MetaClass) classLoader.loadClass(instanceClass);
            Object instance = metaClass.newInstance(new HashMap<>());
            return (T) instance;
        }
        catch (LoadClassException e) {
            throw new NewInstanceException(e);
        }
    }

    public static MetaApplication run(String path){
        context = new ApplicationContext();
        context.setNodes(new HashMap<>());
        context.setConfig(loadConfig(path));
        context.setMetaClassLoader(new MetaClassLoader(new PackageScanner(new ArrayList<>())));
        context.setServer(new Receiver(context.getConfig(),new NodeAddress("localhost",context.getConfig().getPort()), context.getMetaClassLoader().getMetas()));
        context.getServer().start();
        return null;
    }

    public static MetaApplication publish(Class<?> instanceClass) throws LoadClassException {
        MetaPact pact = AnnotationUtil.getMetaPact(instanceClass);
        if(pact == null)throw new LoadClassException(String.format("%s 未定义@Meta", instanceClass.getName()));
        context.getMetaClassLoader().loadClass(instanceClass);
        return null;
    }

    public static MetaApplication defineNode(Object instance, String name, String address) throws TrackException {
        NodeUtil.defineNode(instance, name, address);
        return null;
    }

    public static MetaApplication defineNode(String name, String address) {
        HashMap<String,String> nodes = context.getNodes();
        nodes.remove(name);
        nodes.put(name,address);
        return null;
    }

    private static ApplicationConfig loadConfig(String path){
        ApplicationConfig config = null;
        File file = new File(path);
        if(file.exists()){
            try {
                config = SerializeUtil.yaml.load(new FileInputStream(file));
            }
            catch (FileNotFoundException e) {
                log.error(e);
            }
        }
        if(config == null){
            config = new ApplicationConfig();
            try {
                String data = SerializeUtil.yaml.dump(config);
                new FileOutputStream(file).write(data.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.error(e);
            }
        }
        return config;
    }
}
