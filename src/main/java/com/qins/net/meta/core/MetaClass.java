package com.qins.net.meta.core;

import com.qins.net.core.aop.EventManager;
import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.instance.InstanceManager;
import com.qins.net.meta.annotation.Meta;
import com.qins.net.node.annotation.NodeMapping;
import com.qins.net.node.annotation.NodeMappings;
import com.qins.net.request.core.Request;
import com.qins.net.service.core.Service;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Getter
@Setter
public abstract class MetaClass extends BaseClass {
    protected Request request;
    protected Service service;
    protected HashMap<String, MetaField> metas = new HashMap<>();
    protected EventManager eventManager = new EventManager();
    protected InstanceManager instanceManager = new InstanceManager();
    protected String name;
    protected Class<?> proxyClass;
    protected HashMap<String,String> nodes = new HashMap<>();
    protected HashSet<String> defaultNodes = new HashSet<>();

    protected void linkMetas() {
        for (MetaField metaField : fields.values()){
            if(metaField.getField().getType().getAnnotation(Meta.class) != null){
                metas.put(metaField.name,metaField);
            }
        }
    }

    public MetaClass(Class<?> instanceClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(instanceClass);
        Meta meta = instanceClass.getAnnotation(Meta.class);
        if(meta != null){
            name = "".equals(meta.value()) ? instanceClass.getSimpleName() : meta.value();
            for (Map.Entry<String,String> item : MetaApplication.getContext().getNodes().entrySet()){
                nodes.putIfAbsent(item.getKey(),item.getValue());
            }
            if(meta.nodes().length != 0){
                defaultNodes = new HashSet<>(Arrays.asList(meta.nodes()));
            }
            service = components.service().getConstructor(MetaClass.class).newInstance(this);
            request = components.request().getConstructor(MetaClass.class).newInstance(this);
            for (Annotation annotations : instanceClass.getAnnotations()){
                if(annotations instanceof NodeMappings){
                    for (NodeMapping nodeMapping : ((NodeMappings) annotations).value()){
                        nodes.put(nodeMapping.name(),nodeMapping.host());
                    }
                }
            }
            linkMetas();
        }
    }
    public abstract <T> T newInstance(HashMap<String,String> nodes) throws NewInstanceException;
    public abstract <T> T newInstance(String rawInstance) throws NewInstanceException, InstantiationException, IllegalAccessException;
}
