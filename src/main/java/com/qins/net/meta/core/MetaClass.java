package com.qins.net.meta.core;

import com.qins.net.core.aop.EventManager;
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
    protected void linkMetas() {
        for (MetaField metaField : fields.values()){
            BaseClass metaClass = null;
            if(metaField.getElementClass() != null){
                if(metaField.getElementClass() instanceof MetaClass){
                    metas.put(metaField.name,metaField);
                    metaClass = metaField.getElementClass();
                }
            }
            else {
                if(metaField.getField().getType().getAnnotation(Meta.class) != null){
                    metas.put(metaField.name,metaField);
                    metaClass = metaField.getBaseClass();
                }
            }
            if(metaClass != null){
                for (Annotation annotations : metaClass.getInstanceClass().getAnnotations()){
                    if(annotations instanceof NodeMappings){
                        for (NodeMapping nodeMapping : ((NodeMappings) annotations).value()){
                            nodes.put(nodeMapping.name(),nodeMapping.host());
                        }
                    }
                }
            }
        }
    }

    public MetaClass(Class<?> instanceClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(instanceClass);
        Meta meta = instanceClass.getAnnotation(Meta.class);
        if(meta != null){
            name = "".equals(meta.name()) ? instanceClass.getSimpleName() : meta.name();
            service = components.service().getConstructor(MetaClass.class).newInstance(this);
            request = components.request().getConstructor(MetaClass.class).newInstance(this);
            linkMetas();
        }
    }
    public abstract <T> T newInstance(HashMap<String,String> nodes) throws NewInstanceException;
    public abstract <T> T newInstance(String rawInstance) throws NewInstanceException;
}
