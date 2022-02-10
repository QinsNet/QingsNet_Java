package com.ethereal.meta.core.boot;

import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.net.network.http.server.Http2Server;
import com.ethereal.meta.util.SerializeUtil;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;

import javax.naming.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

public class MetaApplication {
    @Getter
    private ApplicationContext context;

    public ApplicationContext load(String mapping,Class<?> instanceClass){
        context.getServer().start();
        context.getRoot().getMetas().put(mapping,context.getRoot());
        return context;
    }
    public static MetaApplication run(){
        MetaApplication application = new MetaApplication();
        application.context = new ApplicationContext();
        application.loadConfig(ApplicationConfig.class);
        application.context.setServer(new Http2Server(application.context.getConfig(),application.context.getRoot()));
        application.context.getServer().start();
        return application;
    }

    public boolean loadConfig(Class<? extends ApplicationConfig> configClass){
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
            return true;
        }
        else {
            Console.error("未找到application.yaml");
            return false;
        }
    }
}
