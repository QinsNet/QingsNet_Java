package com.ethereal.meta.core.boot;

import com.ethereal.meta.core.entity.TrackException;
import com.ethereal.meta.meta.Meta;
import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.net.network.http.server.Http2Server;
import com.ethereal.meta.util.SerializeUtil;
import lombok.Getter;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MetaApplication {
    @Getter
    private ApplicationContext context;

    public Meta publish(Class<?> instanceClass){
        MetaMapping metaMapping  = instanceClass.getAnnotation(MetaMapping.class);
        if(metaMapping == null){
            context.getRoot().onException(TrackException.ExceptionCode.NotFoundMeta,instanceClass.getName() + "未标记MetaMapping");
            return null;
        }
        return Meta.newMeta(context.getRoot(),metaMapping.value(), instanceClass);
    }
    public static MetaApplication run(){
        MetaApplication application = new MetaApplication();
        application.context = new ApplicationContext();
        application.loadConfig();
        application.context.setServer(new Http2Server(application.context.getConfig(),application.context.getRoot()));
        application.context.getServer().start();
        return application;
    }

    private boolean loadConfig(){
        File file = new File("src/main/resources/application.yaml");
        if(file.exists()){
            try {
                context.setConfig(SerializeUtil.yaml.load(new FileInputStream(file)));
                return true;
            }
            catch (FileNotFoundException e) {
                context.getRoot().onException(e);
                return false;
            }
        }
        else {
            context.setConfig(new ApplicationConfig());
            try {
                String data = SerializeUtil.yaml.dump(context.getConfig());
                new FileOutputStream(file).write(data.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                context.getRoot().onException(e);
                return false;
            }
        }
        return true;
    }
}
