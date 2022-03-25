package com.qins.net.meta.core;

import com.qins.net.core.aop.EventManager;
import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.instance.InstanceManager;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.instance.Meta;
import com.qins.net.meta.annotation.instance.MetaPact;
import com.qins.net.node.annotation.NodeMapping;
import com.qins.net.node.annotation.NodeMappings;
import com.qins.net.request.core.Request;
import com.qins.net.service.core.Service;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Getter
@Setter
public abstract class MetaClass extends BaseClass {
    protected Request request;
    protected Service service;
    protected EventManager eventManager = new EventManager();
    protected InstanceManager instanceManager = new InstanceManager();
    protected Class<?> proxyClass;
    protected Set<String> defaultNodes;
    protected Map<String, String> nodes = new HashMap<>();
    protected Map<String, MetaField> fields = new HashMap<>();
    protected Components components;

    public MetaClass(String name,Class<?> instanceClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name,instanceClass);
        this.components = instanceClass.getAnnotation(Components.class);
        if(components == null)this.components = Components.class.getAnnotation(Components.class);
        MetaPact pact = AnnotationUtil.getMetaPact(instanceClass);
        assert pact != null;
        for (Map.Entry<String,String> item : MetaApplication.getContext().getNodes().entrySet()){
            nodes.putIfAbsent(item.getKey(),item.getValue());
        }
        if(pact.getNodes() != null){
            defaultNodes = pact.getNodes();
        }
        for (Annotation annotations : instanceClass.getAnnotations()){
            if(annotations instanceof NodeMappings){
                for (NodeMapping nodeMapping : ((NodeMappings) annotations).value()){
                    nodes.put(nodeMapping.name(),nodeMapping.host());
                }
            }
        }
    }
    public abstract <T> T newInstance(Map<String,String> nodes) throws NewInstanceException;

}
