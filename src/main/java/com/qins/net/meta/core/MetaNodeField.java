package com.qins.net.meta.core;

import com.qins.net.core.aop.EventManager;
import com.qins.net.core.entity.NodeAddress;
import com.qins.net.core.instance.InstanceManager;
import com.qins.net.request.core.Request;
import com.qins.net.request.core.RequestInterceptor;
import com.qins.net.service.core.Service;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public abstract class MetaNodeField extends MetaClass{
    @Getter
    protected Request request;
    @Getter
    protected Service service;
    @Getter
    protected HashMap<String, MetaNodeField> metas = new HashMap<>();
    @Getter
    protected EventManager eventManager = new EventManager();
    @Getter
    protected InstanceManager instanceManager = new InstanceManager();
    @Getter
    protected String mapping;
    @Getter
    protected Class<?> proxyClass;
    @Getter
    protected Class<?> collectionClass;
    @Getter
    protected Field field;
    @Getter
    protected String name;
    private void onLink() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Field field : AnnotationUtil.getFields(instanceClass, com.qins.net.meta.annotation.Meta.class)){
            field.setAccessible(true);
            com.qins.net.meta.annotation.Meta metaAnnotation = field.getAnnotation(com.qins.net.meta.annotation.Meta.class);
            String name = metaAnnotation.value() != null? metaAnnotation.value() : field.getName();
            MetaNodeField metaNodeField = this.components.metaNode().getConstructor(Class.class)
                    .newInstance(field.getType());
            metaNodeField.name = name;
            metaNodeField.mapping = this.mapping + "/" + mapping;
            if(metaAnnotation.elementClass() != null){
                metaNodeField.collectionClass = instanceClass;
                metaNodeField.instanceClass = metaAnnotation.elementClass();
            }
            metas.put(metaNodeField.name, metaNodeField);
            com.qins.net.meta.annotation.Sync syncAnnotation = field.getAnnotation(com.qins.net.meta.annotation.Sync.class);
            super.name = syncAnnotation.value() != null? syncAnnotation.value() : field.getName();
        }
    }

    public void bindInstance(Factory instance, Request request, NodeAddress local, NodeAddress remote){
        instance.setCallback(0, NoOp.INSTANCE);
        instance.setCallback(1,new RequestInterceptor(request,local,remote));
    }

    public MetaNodeField(Class<?> instance) {
        super(instance);
        try {
            //Life Cycle
            service = components.service().getConstructor(MetaNodeField.class).newInstance(this);
            request = components.request().getConstructor(MetaNodeField.class).newInstance(this);
            //Proxy Instance
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(instanceClass);
            enhancer.setCallbackTypes(new Class[]{NoOp.class,RequestInterceptor.class});
            enhancer.setCallbackFilter(method ->
            {
                if(request.getMethods().containsKey(AnnotationUtil.getMethodPact(method).getMapping())){
                    return 1;
                }
                else return 0;
            });
            proxyClass = enhancer.createClass();
            //Life Cycle
            onLink();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            onException(e);
        }
    }
    public <T> T newInstance(NodeAddress local, NodeAddress remote) {
        return newInstance(null,local,remote);
    }
    public <T> T newInstance(String rawInstance,NodeAddress local, NodeAddress remote) {
        try {
            Factory instance = (Factory) deserialize(rawInstance);
            bindInstance(instance,request,local,remote);
            for (MetaNodeField metaNodeField : metas.values()){
                if(metaNodeField.getField().get(instance) == null){
                    if(metaNodeField.collectionClass != null){
                        metaNodeField.getField().set(instance, metaNodeField.collectionClass.newInstance());
                    }
                    else metaNodeField.getField().set(instance, metaNodeField.newInstance(local,remote));
                }
                if (metaNodeField.collectionClass != null){
                    if(Iterable.class.isAssignableFrom(metaNodeField.collectionClass)){
                        for (Factory item:(Iterable<Factory>) metaNodeField.getField().get(instance)){
                            bindInstance(item, request,local,remote);
                        }
                    }
                    else if(Map.class.isAssignableFrom(metaNodeField.collectionClass)){
                        for(Map.Entry<Object,Object> item:((Map<Object,Object>)(metaNodeField.getField().get(instance))).entrySet()){
                            if(item.getKey().getClass().isAssignableFrom(Factory.class)){
                                bindInstance((Factory) item.getKey(), request, local,remote);
                            }
                            else if(item.getValue().getClass().isAssignableFrom(Factory.class)){
                                bindInstance((Factory) item.getValue(), request,local,remote);
                            }
                        }
                    }
                }
            }
            return (T)instance;
        }
        catch (Exception e){
            onException(e);
            return null;
        }
    }
}
