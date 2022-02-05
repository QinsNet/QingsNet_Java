package com.ethereal.meta.core.boot;

import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.net.network.http.server.Http2Server;
import com.ethereal.meta.util.SerializeUtil;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

public class MetaApplication {
    @Getter
    private static ApplicationContext context;
    public static ApplicationContext publish(String mapping,Class<?> instanceClass){
        if(context == null){
            context = new ApplicationContext();
            loadConfig(ServerConfig.class);
            context.setServer(new Http2Server((ServerConfig) context.getConfig()));
            context.getServer().start();
        }
        Meta meta = Meta.newInstance(instanceClass);
        Meta.getRoot().put(mapping,meta);
        return context;
    }

    public static boolean loadConfig(Class<? extends ApplicationConfig> configClass){
        File file = new File("application.yaml");
        if(file.exists()){
            try {
                context.setConfig(SerializeUtil.yaml.load(new FileInputStream(file)));
            }
            catch (FileNotFoundException e) {
                try {
                    context.setConfig(configClass.getConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                    Console.error(e.getLocalizedMessage());
                }
            }
        }
    }
}
